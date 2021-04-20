package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.ReadWriteAccessDomainData;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ReadWriteAccessDomainDataCacheTest extends BaseCoreFunctionalTestCase {

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[]{ReadWriteAccessDomainData.class};
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.DIALECT, H2Dialect.class.getName());
        cfg.setProperty(Environment.DRIVER, org.h2.Driver.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.USE_QUERY_CACHE, "false");
        cfg.setProperty(Environment.SHOW_SQL, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
    }

    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testGetById_whenItemIsSavedAndCommitted_thenCachePutIsExecutedAndCacheHitHappens() {
        // given
        Statistics stats = sessionFactory().getStatistics();
        CacheRegionStatistics domainDataRegionStatistics = stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME);
        Long id = null;
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData item = new ReadWriteAccessDomainData(System.currentTimeMillis(),  "domainData" );
        id = (Long) s.save( item );
        s.flush();
        s.getTransaction().commit();
        s.close();

        // when
        Assert.assertEquals(0, domainDataRegionStatistics.getMissCount());
        Assert.assertEquals(1, domainDataRegionStatistics.getPutCount());
        Assert.assertEquals(0, domainDataRegionStatistics.getHitCount());


        // when
        s = openSession();
        s.beginTransaction();
        item = (ReadWriteAccessDomainData) s.get(ReadWriteAccessDomainData.class, id);
        Assert.assertEquals("domainData", item.getName());
        s.getTransaction().commit();
        s.close();

        // then
        Assert.assertEquals(0, domainDataRegionStatistics.getMissCount());
        Assert.assertEquals(1, domainDataRegionStatistics.getPutCount());
        Assert.assertEquals(1, domainDataRegionStatistics.getHitCount());

        // test data clear
        s = openSession();
        s.beginTransaction();
        s.flush();
        s.delete(s.get(ReadWriteAccessDomainData.class, id));
        s.getTransaction().commit();
        s.close();
    }

    @Test
    public void testGetById_whenItemIsSavedAndRolledBack_thenCachePutIsNotExecutedAndCacheMissHappens() {
        Statistics stats = sessionFactory().getStatistics();
        Long id = null;
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData item = new ReadWriteAccessDomainData(System.currentTimeMillis(), "domainData");
        id = (Long) s.save( item );
        s.flush();
        s.getTransaction().rollback();
        s.close();

        s = openSession();
        s.beginTransaction();
        item = s.get(ReadWriteAccessDomainData.class, id);
        Assert.assertNull(item);
        s.getTransaction().commit();
        s.close();

        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getMissCount());
    }

    @Test
    public void testNaturalId_whenNaturalIdIsSet_thenTheNaturalIdShouldBeCachePutAndCacheHit() {
        Statistics stats = sessionFactory().getStatistics();
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainData = new ReadWriteAccessDomainData(System.currentTimeMillis(), "domainData");
        final long naturalId = readWriteAccessDomainData.getNaturalId();
        final String naturalId2 = readWriteAccessDomainData.getNaturalId2();
        Long id = (Long) s.save(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();

        Assert.assertEquals(1, stats.getNaturalIdStatistics(ReadWriteAccessDomainData.class.getName()).getCachePutCount());
        
        s = openSession();
        s.beginTransaction();
        readWriteAccessDomainData = s.byNaturalId(ReadWriteAccessDomainData.class)
                .using("naturalId", naturalId)
                .using("naturalId2", naturalId2)
                .load();
        assertThat(readWriteAccessDomainData).isNotNull();
        s.getTransaction().commit();
        s.close();
        
        Assert.assertEquals(1, stats.getNaturalIdStatistics(ReadWriteAccessDomainData.class.getName()).getCacheHitCount());

        // test data clear
        s = openSession();
        s.beginTransaction();
        s.flush();
        ReadWriteAccessDomainData toDelete = s.get(ReadWriteAccessDomainData.class, id);
        s.delete(toDelete);
        s.getTransaction().commit();
        s.close();
    }
    
    @Test
    public void testUpdate_whenUpdateIsRolledBack_thenCacheShouldNotUpdate() {
        Statistics stats = sessionFactory().getStatistics();
        Long id = null;
        Session s = openSession();
        s.beginTransaction();
        String expectedDomainDataNameAfterRollback = "domainData";
        ReadWriteAccessDomainData item = new ReadWriteAccessDomainData(System.currentTimeMillis(), expectedDomainDataNameAfterRollback);
        id = (Long) s.save( item );
        s.flush();
        s.getTransaction().commit();
        s.close();
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());

        s = openSession();
        s.beginTransaction();
        item = (ReadWriteAccessDomainData) s.get(ReadWriteAccessDomainData.class, id);
        item.setName("newDomainData");
        s.update(item);
        s.flush();
        s.getTransaction().rollback();
        s.clear();
        s.close();

        s = openSession();
        s.beginTransaction();
        item = s.get(ReadWriteAccessDomainData.class, id);
        Assert.assertEquals(expectedDomainDataNameAfterRollback, item.getName());
        s.getTransaction().commit();
        s.close();

        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());

        // test data clear
        s = openSession();
        s.beginTransaction();
        s.flush();
        s.delete(s.get(ReadWriteAccessDomainData.class, id));
        s.getTransaction().commit();
        s.close();
    }

    @Test
    public void testUpdate_whenUpdateIsCommitted_thenCacheShouldBeUpdated() {
        Statistics stats = sessionFactory().getStatistics();
        Long id = null;
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainData = new ReadWriteAccessDomainData(System.currentTimeMillis(), "domainData");
        id = (Long) s.save(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();
        s.close();
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());

        s = openSession();
        s.beginTransaction();
        readWriteAccessDomainData = s.get(ReadWriteAccessDomainData.class, id);
        String updatedDomainDataName = "newDomainData";
        readWriteAccessDomainData.setName(updatedDomainDataName);
        s.update(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();
        s.clear();
        s.close();
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(2, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());

        s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainDataAfterUpdate = s.get(ReadWriteAccessDomainData.class, id);
        s.getTransaction().commit();
        s.close();

        assertThat(readWriteAccessDomainDataAfterUpdate.getName()).isEqualTo(updatedDomainDataName);
        Assert.assertEquals(2, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(2, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());

        // test data clear
        s = openSession();
        s.beginTransaction();
        s.flush();
        s.delete(s.get(ReadWriteAccessDomainData.class, id));
        s.getTransaction().commit();
        s.close();
        stats.logSummary();
    }

    @Test
    public void testDelete_whenDeleteIsCommitted_thenCacheShouldBeMiss() {
        Statistics stats = sessionFactory().getStatistics();
        Long id = null;
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainData = new ReadWriteAccessDomainData(System.currentTimeMillis(), "domainData");
        id = (Long) s.save(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();
        s.close();
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());

        s = openSession();
        s.beginTransaction();
        readWriteAccessDomainData = s.get(ReadWriteAccessDomainData.class, id);
        s.delete(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();
        s.clear();
        s.close();
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getMissCount());

        s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainDataAfterDelete = s.get(ReadWriteAccessDomainData.class, id);
        s.getTransaction().commit();
        s.close();
        // delete 된 아이템의  SoftLockImpl이 캐쉬에 남아있을때 로그에 cacheMiss로 안찍히지만  stats 에서 cacheMiss로 인식됨
        assertThat(readWriteAccessDomainDataAfterDelete).isNull();
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getPutCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(ReadWriteAccessDomainData.CACHE_REGION_NAME).getMissCount());
    }
}
