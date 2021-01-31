package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.TransactionalAccessDomainData;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 
 * @author Nikita Koksharov
 *
 */
public class TransactionalAccessQueryCacheTest extends BaseCoreFunctionalTestCase {

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
        cfg.setProperty(Environment.USE_QUERY_CACHE, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
    }
    
    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testNamedQueryCache_whenQueryIsSetToCacheable_thenCachePutShouldBeExecutedAndCacheHitShouldBeForTheNextQuery() {
        Statistics stats = sessionFactory().getStatistics();

        final String givenDomainDataNameToQuery = "transactional";
        final String queryCacheRegionName = "queryCacheRegion";
        Session s = openSession();
        s.beginTransaction();
        Long idAndNaturalId = System.currentTimeMillis();
        TransactionalAccessDomainData transactionalAccessDomainData = new TransactionalAccessDomainData(idAndNaturalId, givenDomainDataNameToQuery);
        s.save(transactionalAccessDomainData);
        s.flush();
        s.getTransaction().commit();

        s = openSession();
        s.beginTransaction();
        Query query1 = s.getNamedQuery(TransactionalAccessDomainData.NAMED_QUERY_NAME);
        query1.setCacheable(true);
        query1.setCacheRegion(queryCacheRegionName);
        query1.setParameter("name", givenDomainDataNameToQuery);
        query1.uniqueResult();
        s.getTransaction().commit();
        s.close();

        CacheRegionStatistics queryCacheRegionStat = stats.getDomainDataRegionStatistics(queryCacheRegionName);
        Assert.assertEquals(1, queryCacheRegionStat.getPutCount());
        Assert.assertEquals(0, queryCacheRegionStat.getHitCount());


        s = openSession();
        s.beginTransaction();
        Query query2 = s.getNamedQuery(TransactionalAccessDomainData.NAMED_QUERY_NAME);
        query2.setCacheable(true);
        query2.setCacheRegion(queryCacheRegionName);
        query2.setParameter("name", givenDomainDataNameToQuery);
        query2.uniqueResult();
        s.getTransaction().commit();
        s.close();
        Assert.assertEquals(1, queryCacheRegionStat.getPutCount());
        Assert.assertEquals(1, queryCacheRegionStat.getHitCount());
    }
}
