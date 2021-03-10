package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TransactionAccessDomainDataRegionGroupTest extends BaseCoreFunctionalTestCase {

    private final String collectionCacheRegionName = "hibernate.CollectionCacheTest$ParentDomainData.children";

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[]{DomainRegionOne.class, DomainRegionTwo.class};
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.DRIVER, org.h2.Driver.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");

        cfg.setProperty(Environment.SHOW_SQL, "true");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
        cfg.setProperty("hibernate.cache.arcus.evictionRegionGroupOnCacheUpdate", DomainRegionOne.regionName + "," + DomainRegionTwo.regionName);
    }

    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testCacheEvictOnCachePut_whenADomainRegionOneEntityIsDeleted_thenDomainRegionTwoCacheWithSameIdIsEvicted() {
        CacheRegionStatistics regionOneStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionOne.regionName);
        CacheRegionStatistics regionTwoStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionTwo.regionName);
        final long id = System.currentTimeMillis();
        Session s = openSession();
        s.beginTransaction();
        DomainRegionOne regionOne = new DomainRegionOne(id);
        DomainRegionTwo regionTwo = new DomainRegionTwo(id);
        s.save(regionOne);
        s.save(regionTwo);
        s.flush();
        s.getTransaction().commit();
        s.close();

        assertEquals(1, regionOneStat.getPutCount());
        assertEquals(0, regionOneStat.getHitCount());
        assertEquals(1, regionTwoStat.getPutCount());
        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneFromCache = s.get(DomainRegionOne.class, id);
        s.delete(domainRegionOneFromCache);
        s.getTransaction().commit();
        s.close();
        assertEquals(1, regionOneStat.getHitCount());
        System.out.println("============================");

        assertEquals(0, regionTwoStat.getMissCount());
        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneAfterDelete = s.get(DomainRegionOne.class, id);
        s.get(DomainRegionTwo.class, id);
        s.getTransaction().commit();
        s.close();
        assertNull(domainRegionOneAfterDelete);
        assertEquals(1, regionTwoStat.getMissCount());
    }

    /**
     * When update of regionOneEntity and delete of the regionTwo are executed in a transaction, delete is called after update and the cache item that the update put is evicted.
     * As a result, the next get operation of the updated entity will get cacheMiss
     */
    @Test
    public void testCacheEvictOnCachePut_whenDomainRegionOneEntityIsDeletedAndDomainRegionTwoIsUpdatedInTheSameTransaction_thenDomainRegionTwoShouldBeCacheMiss() {
        CacheRegionStatistics regionOneStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionOne.regionName);
        CacheRegionStatistics regionTwoStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionTwo.regionName);
        final long id = System.currentTimeMillis();
        Session s = openSession();
        s.beginTransaction();
        DomainRegionOne regionOne = new DomainRegionOne(id);
        DomainRegionTwo regionTwo = new DomainRegionTwo(id);
        s.save(regionOne);
        s.save(regionTwo);
        s.flush();
        s.getTransaction().commit();
        s.close();

        assertEquals(1, regionOneStat.getPutCount());
        assertEquals(0, regionOneStat.getHitCount());
        assertEquals(1, regionTwoStat.getPutCount());
        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneFromCache = s.get(DomainRegionOne.class, id);
        s.delete(domainRegionOneFromCache);
        DomainRegionTwo domainRegionTwo = s.get(DomainRegionTwo.class, id);
        domainRegionTwo.value = "updated";
        s.update(domainRegionTwo);
        s.flush();
        s.getTransaction().commit();
        s.close();
        assertEquals(1, regionOneStat.getHitCount());
        assertEquals(0, regionTwoStat.getMissCount());
        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneAfterDelete = s.get(DomainRegionOne.class, id);
        s.get(DomainRegionTwo.class, id);
        s.getTransaction().commit();
        s.close();
        assertNull(domainRegionOneAfterDelete);
        assertEquals(1, regionTwoStat.getMissCount());
    }


    @Test
    public void testCacheEvictOnCachePut_whenADomainRegionOneEntityIsUpdated_thenDomainRegionTwoCacheWithSameIdIsEvicted() {
        CacheRegionStatistics regionOneStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionOne.regionName);
        CacheRegionStatistics regionTwoStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionTwo.regionName);
        final long id = System.currentTimeMillis();
        Session s = openSession();
        s.beginTransaction();
        DomainRegionOne regionOne = new DomainRegionOne(id);
        DomainRegionTwo regionTwo = new DomainRegionTwo(id);
        s.save(regionOne);
        s.save(regionTwo);
        s.flush();
        s.getTransaction().commit();
        s.close();

        assertEquals(1, regionOneStat.getPutCount());
        assertEquals(1, regionTwoStat.getPutCount());
        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneFromCache = s.get(DomainRegionOne.class, id);
        domainRegionOneFromCache.value = "updated-value";
        s.update(domainRegionOneFromCache);
        s.getTransaction().commit();
        s.close();
        assertEquals(1, regionOneStat.getHitCount());

        System.out.println("============================");

        assertEquals(0, regionTwoStat.getMissCount());
        s = openSession();
        s.beginTransaction();
        s.get(DomainRegionTwo.class, id);
        s.getTransaction().commit();
        s.close();
        assertEquals(1, regionTwoStat.getMissCount());
    }

    /**
     * When updates of regionOne and regionTwo are executed in a transaction, the cache items of both region entities are put.
     * As a result, the next get operation of both will get cacheHit
     */
    @Test
    public void testCacheEvictOnCachePut_whenDomainRegionOneEntityAndDomainRegionOneEntityAreUpdatedInTheSameTransaction_thenDomainRegionOneAndDomainRegionTwoShouldBeCacheHit() {
        CacheRegionStatistics regionOneStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionOne.regionName);
        CacheRegionStatistics regionTwoStat = sessionFactory()
                .getStatistics().getDomainDataRegionStatistics(DomainRegionTwo.regionName);
        final long id = System.currentTimeMillis();
        Session s = openSession();
        s.beginTransaction();
        DomainRegionOne regionOne = new DomainRegionOne(id);
        DomainRegionTwo regionTwo = new DomainRegionTwo(id);
        s.save(regionOne);
        s.save(regionTwo);
        s.flush();
        s.getTransaction().commit();
        s.close();

        assertEquals(1, regionOneStat.getPutCount());
        assertEquals(1, regionTwoStat.getPutCount());
        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        DomainRegionOne domainRegionOneFromCache = s.get(DomainRegionOne.class, id);
        domainRegionOneFromCache.value = "updated-value";
        s.update(domainRegionOneFromCache);
        DomainRegionTwo domainRegionTwoFromCache = s.get(DomainRegionTwo.class, id);
        domainRegionTwoFromCache.value = "updated";
        s.update(domainRegionTwoFromCache);
        s.getTransaction().commit();
        s.close();
        assertEquals(1, regionOneStat.getHitCount());
        assertEquals(1, regionTwoStat.getHitCount());

        System.out.println("============================");

        s = openSession();
        s.beginTransaction();
        s.get(DomainRegionOne.class, id);
        s.get(DomainRegionTwo.class, id);
        s.getTransaction().commit();
        s.close();
        assertEquals(2, regionOneStat.getHitCount());
        assertEquals(2, regionTwoStat.getHitCount());
    }

    @Entity
    @NoArgsConstructor
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = DomainRegionOne.regionName)
    public static class DomainRegionOne implements Serializable {
        public static final String regionName = "DomainRegionOne";

        @Id
        Long id;

        public DomainRegionOne(Long id) {
            this.id = id;
        }

        public String value = "";
    }

    @Entity
    @NoArgsConstructor
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = DomainRegionTwo.regionName)
    public static class DomainRegionTwo implements Serializable {
        public static final String regionName = "DomainRegionTwo";

        @Id
        long id;
        String value;
        public DomainRegionTwo(long id) {
            this.id = id;
        }
    }
}
