package hibernate;

import com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory;
import hibernate.domain.PAccount;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PAccountCacheTest extends BaseCoreFunctionalTestCase {

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[]{PAccount.class};
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
    public void test_whenMultipleTransactionUpdatesAtTheSameTime_thenCacheItemAndDbEntityAreIdentical() throws InterruptedException {
        Statistics stats = sessionFactory().getStatistics();
        Long pAccountId = System.currentTimeMillis();

        //1) PAccount를 Insert한다
        Session s = openSession();
        s.beginTransaction();
        PAccount pAccount = new PAccount(pAccountId, 0, "HOLD");
        pAccountId = (Long) s.save(pAccount);
        s.flush();
        s.getTransaction().commit();
        s.close();
        assertThat(stats.getDomainDataRegionStatistics(PAccount.CACHE_REGION_NAME).getPutCount()).isEqualTo(1);

        //2) lastAccessesAt과 status를 동시에 다른 트랜잭션에서 업데이트 한다.
        String statusToUpdate = "OK";
        int lastAccessAtToUpdate = 1;
        updateStatusAndLastAccessAtInDistinctTransactions(pAccountId, lastAccessAtToUpdate, statusToUpdate);
        assertThat(stats.getDomainDataRegionStatistics(PAccount.CACHE_REGION_NAME).getHitCount()).isEqualTo(2);

        //3) 업데이트 후 실제 데이터를 DB에서 가져온다
        s = openSession();
        s.beginTransaction();
        PAccount resultFromDB = s.get(PAccount.class, pAccountId);
        s.getTransaction().commit();
        s.close();

        //4) 결과 값과 업데이트 값과 비교해본다
        assertThat(resultFromDB.getLastAccessAt()).isEqualTo(lastAccessAtToUpdate);
        assertThat(resultFromDB.getStatus()).isEqualTo(statusToUpdate);
        assertThat(stats.getDomainDataRegionStatistics(PAccount.CACHE_REGION_NAME).getPutCount()).isEqualTo(2);

        // 추가
        //5) 데이터를 Cache에서 가져온다
        s = openSession();
        s.beginTransaction();
        PAccount resultFromCache = s.get(PAccount.class, pAccountId);
        s.getTransaction().commit();
        s.close();

        //6) 결과 값과 업데이트 값과 비교해본다
        assertThat(resultFromCache.getLastAccessAt()).isEqualTo(lastAccessAtToUpdate);
        assertThat(resultFromCache.getStatus()).isEqualTo(statusToUpdate);
        assertThat(stats.getDomainDataRegionStatistics(PAccount.CACHE_REGION_NAME).getHitCount()).isEqualTo(3);
    }

    private void updateStatusAndLastAccessAtInDistinctTransactions(Long id, int lastAccessAt, String status) throws InterruptedException {
        Callable<PAccount> lastAccessAtUpdateInTransaction = () -> {
            Session s;
            PAccount pAccount;
            s = openSession();
            s.beginTransaction();
            pAccount = s.get(PAccount.class, id);
            pAccount.setLastAccessAt(lastAccessAt);
            s.flush();
            s.getTransaction().commit();
            s.clear();
            s.close();
            return pAccount;
        };

        Callable<PAccount> statusUpdateInTransaction = () -> {
            Session s;
            PAccount data;
            s = openSession();
            s.beginTransaction();
            data = s.get(PAccount.class, id);
            data.setStatus(status);
            s.flush();
            s.getTransaction().commit();
            s.clear();
            s.close();
            return data;
        };

        List<Callable<PAccount>> callables = new ArrayList<>(2);
        callables.add(lastAccessAtUpdateInTransaction);
        callables.add(statusUpdateInTransaction);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.invokeAll(callables);
        Thread.sleep(2000L);
    }
}
