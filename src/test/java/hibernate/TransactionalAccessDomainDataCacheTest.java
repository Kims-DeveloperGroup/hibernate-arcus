package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.TransactionalAccessDomainData;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.NaturalIdStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionalAccessDomainDataCacheTest extends BaseCoreFunctionalTestCase {

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[] { TransactionalAccessDomainData.class};
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.DRIVER, org.h2.Driver.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
    }
    
    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testNaturalIdCache_whenDomainDataIsSavedWithNaturalId_thenCachePutShouldBeExecutedAndCacheHitShouldBeReturnedForTheNextGetOperation() {
        Statistics stats = sessionFactory().getStatistics();
        Session s = openSession();
        s.beginTransaction();
        TransactionalAccessDomainData transactionalAccessDomainData = new TransactionalAccessDomainData(System.currentTimeMillis(), "data");
        transactionalAccessDomainData.setNaturalId(System.currentTimeMillis());
        s.save(transactionalAccessDomainData);
        s.flush();
        s.getTransaction().commit();

        CacheRegionStatistics domainDataRegionStatistics = stats.getDomainDataRegionStatistics(TransactionalAccessDomainData.CACHE_REGION_NAME);
        NaturalIdStatistics naturalIdStatistics = stats.getNaturalIdStatistics(TransactionalAccessDomainData.class.getName());
        Assert.assertEquals(1, domainDataRegionStatistics.getPutCount());
        Assert.assertEquals(1, naturalIdStatistics.getCachePutCount());

        s = openSession();
        s.beginTransaction();
        TransactionalAccessDomainData transactionalAccessDomainDataByNaturalId = s.bySimpleNaturalId(TransactionalAccessDomainData.class).load(transactionalAccessDomainData.getNaturalId());
        s.delete(transactionalAccessDomainDataByNaturalId);
        s.getTransaction().commit();
        s.close();

        assertThat(transactionalAccessDomainDataByNaturalId).isNotNull();
        Assert.assertEquals(1, domainDataRegionStatistics.getHitCount());
        Assert.assertEquals(1, naturalIdStatistics.getCacheHitCount());
    }
    
    @Test
    public void testUpdateWithRefreshThenRollback() {
        Statistics stats = sessionFactory().getStatistics();
        Session s = openSession();
        s.beginTransaction();
        String nameBeforeUpdate = "transactional";
        TransactionalAccessDomainData transactionalAccessDomainData = new TransactionalAccessDomainData(System.currentTimeMillis(), nameBeforeUpdate);
        Long id = (Long) s.save(transactionalAccessDomainData);
        s.flush();
        s.getTransaction().commit();

        CacheRegionStatistics domainDataRegionStatistics = stats.getDomainDataRegionStatistics(TransactionalAccessDomainData.CACHE_REGION_NAME);
        Assert.assertEquals(1, domainDataRegionStatistics.getPutCount());
        Assert.assertEquals(0, domainDataRegionStatistics.getHitCount());

        s = openSession();
        s.beginTransaction();
        transactionalAccessDomainData = s.get(TransactionalAccessDomainData.class, id);
        transactionalAccessDomainData.setName("newTransactional");
        s.update(transactionalAccessDomainData);
        s.flush();
        s.refresh(transactionalAccessDomainData);
        s.getTransaction().rollback();
        s.clear();
        s.close();

        Assert.assertEquals(1, domainDataRegionStatistics.getHitCount());
    }
}