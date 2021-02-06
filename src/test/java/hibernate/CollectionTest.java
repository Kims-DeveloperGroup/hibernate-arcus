package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionTest extends BaseCoreFunctionalTestCase {

    @Entity
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "ParentDomainData")
    public static class ParentDomainData implements Serializable {
      @Id
      @Column(name = "PARENT_ID")
      Long parentId;

      @Column(name = "UNIQUE_FIELD", unique = true)
      String uniqueField;

      @ManyToMany
      @JoinTable(
          name = "PARENTDOMAINDATA_CHILDDOMAINDATA",
          joinColumns = @JoinColumn(
              name = "UNIQUE_FIELD", referencedColumnName = "UNIQUE_FIELD"),
          inverseJoinColumns = @JoinColumn(
              name = "CHILD_ID", referencedColumnName = "CHILD_ID"))
      @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
      private final List<ChildDomainData> children = new ArrayList<>();
    }

    @Entity
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "ChildDomainData")
    public static class ChildDomainData implements Serializable {
      @Id
      @Column(name = "CHILD_ID")
      long childId;
    }

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[] { ParentDomainData.class, ChildDomainData.class };
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.DRIVER, org.h2.Driver.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");

        cfg.setProperty(Environment.SHOW_SQL, "true");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.USE_QUERY_CACHE, "true");
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, HibernateArcusRegionFactory.class.getName());
    }
    
    @Before
    public void before() {
        sessionFactory().getStatistics().clear();
    }

    @Test
    public void testCollectionCache_whenCollectionCacheIsPut_thenCollectionCacheShouldBeHitForTheNextGet() {
        CacheRegionStatistics collectionTestCacheStats = sessionFactory().getStatistics()
                .getCacheRegionStatistics("hibernate.CollectionTest$ParentDomainData.children");

        Session s = openSession();
        s.beginTransaction();

        ParentDomainData parentDomainData = new ParentDomainData();
        long parentDomainDataId = System.currentTimeMillis();
        parentDomainData.parentId = parentDomainDataId;
        parentDomainData.uniqueField = "1";
        ChildDomainData childDomainData = new ChildDomainData();
        childDomainData.childId = 1L;
        s.save(childDomainData);
        parentDomainData.children.add(childDomainData);
        s.save(parentDomainData);
        s.flush();
        s.getTransaction().commit();

        s = openSession();
        s.beginTransaction();
        ParentDomainData parentDomainData1 = s.get(ParentDomainData.class, parentDomainDataId);
        assertThat(parentDomainData1.children).hasSize(1);
        s.getTransaction().commit();

        Assert.assertEquals(0, collectionTestCacheStats.getHitCount());
        Assert.assertEquals(1, collectionTestCacheStats.getPutCount());


        s = openSession();
        s.beginTransaction();
        ParentDomainData parentDomainData2 = s.get(ParentDomainData.class, parentDomainDataId);
        assertThat(parentDomainData2.children.size()).isEqualTo(1);
        s.getTransaction().commit();
        s.close();

        Assert.assertEquals(1, collectionTestCacheStats.getHitCount());
        Assert.assertEquals(1, collectionTestCacheStats.getPutCount());
    }
}
