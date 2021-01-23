package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.DomainData;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class QueryResultCacheTest extends BaseCoreFunctionalTestCase {

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[] { DomainData.class};
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.DIALECT, H2Dialect.class.getName());
        cfg.setProperty(Environment.DRIVER, org.h2.Driver.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.USE_QUERY_CACHE, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
    }

    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testQueryCache_whenANamedQueryIsExecuted_thenTheNextSameNameQueryShouldBeCacheHit() {
        Statistics stats = sessionFactory().getStatistics();
        // given
        String queryCacheRegionName = "testQueryCache";

        Session s = openSession();
        s.beginTransaction();
        DomainData domainDataStoredInDB = new DomainData("domainData:" + System.currentTimeMillis());
        s.save(domainDataStoredInDB);
        s.flush();
        s.getTransaction().commit();

        // when
        s = openSession();
        s.beginTransaction();
        Query cacheableQuery1 = s.getNamedQuery("domainDataNamedQuery");
        cacheableQuery1.setCacheable(true);
        cacheableQuery1.setCacheRegion(queryCacheRegionName);
        cacheableQuery1.setParameter("name", domainDataStoredInDB.getName());
        cacheableQuery1.uniqueResult();
        s.getTransaction().commit();
        s.close();

        // then
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegionName).getMissCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegionName).getPutCount());

        // when
        s = openSession();
        s.beginTransaction();
        Query cacheableQuery2 = s.getNamedQuery("domainDataNamedQuery");
        cacheableQuery2.setCacheable(true);
        cacheableQuery2.setCacheRegion(queryCacheRegionName);
        cacheableQuery2.setParameter("name", domainDataStoredInDB.getName());
        cacheableQuery2.uniqueResult();
        s.getTransaction().commit();
        s.close();

        // then
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegionName).getHitCount());

        stats.logSummary();
    }

    @Test
    public void testQueryCache_whenDomainDataUpdateRollback_thenTheSameNameQueryCacheShouldNotReferenceTheUpdate() {
        String queryCacheRegion = "testQueryCache";
        Statistics stats = sessionFactory().getStatistics();
        // given
        Session s = openSession();
        s.beginTransaction();
        DomainData domainDataStoredInDB = new DomainData("domainData#" + System.currentTimeMillis());
        s.save(domainDataStoredInDB);
        s.flush();
        s.getTransaction().commit();

        // when
        s = openSession();
        s.beginTransaction();
        Query cacheableQuery1 = s.getNamedQuery("domainDataNamedQuery");
        cacheableQuery1.setCacheable(true);
        cacheableQuery1.setCacheRegion(queryCacheRegion);
        cacheableQuery1.setParameter("name", domainDataStoredInDB.getName());

        DomainData domainDataToUpdateButRollback = (DomainData) cacheableQuery1.uniqueResult();
        domainDataToUpdateButRollback.setName("domainData#" + System.currentTimeMillis());
        s.save(domainDataToUpdateButRollback);
        s.getTransaction().rollback();
        s.close();

        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(queryCacheRegion).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion).getMissCount());

        // then
        s = openSession();
        s.beginTransaction();
        Query query2 = s.getNamedQuery("domainDataNamedQuery");
        query2.setCacheable(true);
        query2.setCacheRegion(queryCacheRegion);
        query2.setParameter("name", domainDataStoredInDB.getName());
        DomainData domainDataFromQuery2 = (DomainData) query2.uniqueResult();
        s.getTransaction().commit();
        s.close();

        assertThat(domainDataFromQuery2.getName()).isEqualTo(domainDataStoredInDB.getName());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion).getHitCount());
    }

    @Test
    public void testQueryCache_whenTheSameNamedQueryWithNonIdenticalParamQueryCacheExists_thenQueryCacheShouldBeCacheMiss() {
        String queryCacheRegion = "testQueryCache";
        Statistics stats = sessionFactory().getStatistics();
        // given
        Session s = openSession();
        s.beginTransaction();
        DomainData domainDataStoredInDB1 = new DomainData("domainData#" + System.currentTimeMillis());
        s.save(domainDataStoredInDB1);

        DomainData domainDataStoredInDB2 = new DomainData("domainData#" + System.currentTimeMillis());
        s.save(domainDataStoredInDB2);
        s.flush();
        s.getTransaction().commit();

        s = openSession();
        s.beginTransaction();
        Query cacheableQuery1 = s.getNamedQuery("domainDataNamedQuery");
        cacheableQuery1.setCacheable(true);
        cacheableQuery1.setCacheRegion(queryCacheRegion);
        cacheableQuery1.setParameter("name", domainDataStoredInDB1.getName());

        DomainData domainDataToUpdateButRollback = (DomainData) cacheableQuery1.uniqueResult();
        domainDataToUpdateButRollback.setName("domainData#" + System.currentTimeMillis());
        s.save(domainDataToUpdateButRollback);
        s.getTransaction().rollback();
        s.close();

        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(queryCacheRegion).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion).getMissCount());

        // when
        s = openSession();
        s.beginTransaction();
        Query query2 = s.getNamedQuery("domainDataNamedQuery");
        query2.setCacheable(true);
        query2.setCacheRegion(queryCacheRegion);
        query2.setParameter("name", domainDataStoredInDB2.getName());
        DomainData domainDataFromQuery2 = (DomainData) query2.uniqueResult();
        s.getTransaction().commit();
        s.close();

        // then
        Assert.assertEquals(2, stats.getDomainDataRegionStatistics(queryCacheRegion).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(queryCacheRegion).getHitCount());
        Assert.assertEquals(2, stats.getDomainDataRegionStatistics(queryCacheRegion).getMissCount());
    }

    @Test
    public void testQueryCache_whenTheSameNamedQueryWithNonIdenticalRegionQueryCacheExists_thenQueryCacheShouldBeCacheMiss() {
        String queryCacheRegion1 = "testQueryCache-1";
        String queryCacheRegion2 = "testQueryCache-2";
        Statistics stats = sessionFactory().getStatistics();
        // given
        Session s = openSession();
        s.beginTransaction();
        DomainData domainDataStoredInDB1 = new DomainData("domainData#" + System.currentTimeMillis());
        s.save(domainDataStoredInDB1);

        DomainData domainDataStoredInDB2 = new DomainData("domainData#" + System.currentTimeMillis());
        s.save(domainDataStoredInDB2);
        s.flush();
        s.getTransaction().commit();

        s = openSession();
        s.beginTransaction();
        Query cacheableQuery1 = s.getNamedQuery("domainDataNamedQuery");
        cacheableQuery1.setCacheable(true);
        cacheableQuery1.setCacheRegion(queryCacheRegion1);
        cacheableQuery1.setParameter("name", domainDataStoredInDB1.getName());

        DomainData domainDataToUpdateButRollback = (DomainData) cacheableQuery1.uniqueResult();
        domainDataToUpdateButRollback.setName("domainData#" + System.currentTimeMillis());
        s.save(domainDataToUpdateButRollback);
        s.getTransaction().rollback();
        s.close();

        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion1).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(queryCacheRegion1).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion1).getMissCount());

        // when
        s = openSession();
        s.beginTransaction();
        Query query2 = s.getNamedQuery("domainDataNamedQuery");
        query2.setCacheable(true);
        query2.setCacheRegion(queryCacheRegion2);
        query2.setParameter("name", domainDataStoredInDB2.getName());
        DomainData domainDataFromQuery2 = (DomainData) query2.uniqueResult();
        s.getTransaction().commit();
        s.close();

        // then
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion2).getPutCount());
        Assert.assertEquals(0, stats.getDomainDataRegionStatistics(queryCacheRegion2).getHitCount());
        Assert.assertEquals(1, stats.getDomainDataRegionStatistics(queryCacheRegion2).getMissCount());
    }
}
