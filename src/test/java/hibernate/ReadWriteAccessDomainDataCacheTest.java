package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.ReadWriteAccessDomainData;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ReadWriteAccessDomainDataCacheTest extends BaseCoreFunctionalTestCase {

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[] { ReadWriteAccessDomainData.class};
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
    public void test_whenMultipleTransactionUpdatesAtTheSameTime_thenCacheItemAndDbEntityAreIdentical() {
        Statistics stats = sessionFactory().getStatistics();
        Long id = null;

        // 데이터를 인서트하고 캐쉬에 넣는다
        Session s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData readWriteAccessDomainData = new ReadWriteAccessDomainData(System.currentTimeMillis(), "0");
        id = (Long) s.save(readWriteAccessDomainData);
        s.flush();
        s.getTransaction().commit();
        s.close();

        int parallel = 20;
        // 이름과 값을 다른 트랜잭션 에서 동시에 실행한다
        try {
            updateNameAndValue(id, parallel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();

        //캐쉬에서 데이터를 가져온다
        s = openSession();
        s.beginTransaction();
        ReadWriteAccessDomainData fromCache = s.get(ReadWriteAccessDomainData.class, id);
        s.getTransaction().commit();
        s.close();


        //디비에서 데이터를 가져온다.
        s = openSession();
        s.beginTransaction();
        Query query = s.getNamedQuery("domainDataNamedQueryById");
        query.setParameter("id", id);
        log.info("fromDB");
        ReadWriteAccessDomainData fromDB = (ReadWriteAccessDomainData) query.uniqueResult();
        log.info("fromDB");
        s.getTransaction().commit();
        s.close();

        // 캐쉬에서
        // value 0 -> 1 확인한다
        // 이름이   updated로 바뀌었는지 확인하다
        assertThat(fromCache.value).isEqualTo(1);
        assertThat(fromCache.getName()).isEqualTo("updated");

        // 디비랑 캐쉬랑 값이 같은지 확인한다
        assertThat(fromCache.value).isEqualTo(fromDB.getValue());
        assertThat(fromCache.getName()).isEqualTo(fromDB.getName());
    }

    private void updateNameAndValue(Long id, int parallel) throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(parallel);

        Callable<ReadWriteAccessDomainData> incrementDataValue = () -> {
            Session s;
            ReadWriteAccessDomainData data;
            s = openSession();
            s.beginTransaction();
            data = s.get(ReadWriteAccessDomainData.class, id);
            log.info("===1 name: {} value: {}", data.getName(), data.value);
            data.value += 1;
            s.flush();
            s.getTransaction().commit();
            s.clear();
            s.close();
            return data;
        };

        Callable<ReadWriteAccessDomainData> setName = () -> {
            Session s;
            ReadWriteAccessDomainData data;
            s = openSession();
            s.beginTransaction();
            data = s.get(ReadWriteAccessDomainData.class, id);
            log.info("===2 name: {} value: {}", data.getName(), data.value);
            data.setName("updated");
            s.flush();
            s.getTransaction().commit();
            s.clear();
            s.close();
            return data;
        };

        List<Callable<ReadWriteAccessDomainData>> callables = new ArrayList<>(parallel);

        for (int i = 0; i < parallel / 2; i++) {
            callables.add(incrementDataValue);
        }

        for (int i = 0; i < parallel / 2; i++) {
            callables.add(setName);
        }

        scheduledExecutorService.invokeAll(callables);
        Thread.sleep(1000L);
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
        //save 할때 entity를 가져오는 과정에서 cacheMiss로 로그가 찍히는데 stats에서는 miss로 인식되지 않음

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
