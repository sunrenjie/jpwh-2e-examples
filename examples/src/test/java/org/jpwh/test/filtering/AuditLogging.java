package org.jpwh.test.filtering;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.interceptor.AuditLogRecord;
import org.jpwh.model.filtering.interceptor.Item;
import org.jpwh.model.filtering.interceptor.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AuditLogging extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringInterceptorPU");
    }

    @Test
    public void writeAuditLog() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {

            Long CURRENT_USER_ID;
            {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                User currentUser = new User("johndoe");
                em.persist(currentUser);
                tx.commit();
                em.close();
                CURRENT_USER_ID = currentUser.getId();
            }

            EntityManagerFactory emf = JPA.getEntityManagerFactory();
            // The previous approach to use JPA EntityManager does not work with property value
            //   org.hibernate.jpa.AvailableSettings.SESSION_INTERCEPTOR or
            //   org.hibernate.cfg.AvailableSettings.SESSION_SCOPED_INTERCEPTOR:
            // EntityManager em = emf.createEntityManager(properties);
            // With this, the interceptor for em will still be the default EmptyInterceptor.
            // So we are forced to switch to Hibernate API around Session interface, as suggested in the official doc:
            // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#events-interceptors
            SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
            Session session = sessionFactory
                    .withOptions()
                    .interceptor(new AuditLogInterceptor())
                    .openSession();

            // Manually fetch and configure the interceptor later, such that it is separated from its creation.
            AuditLogInterceptor interceptor = (AuditLogInterceptor) ((SessionImplementor) session).getInterceptor();
            interceptor.setCurrentSession(session);
            interceptor.setCurrentUserId(CURRENT_USER_ID);

            tx.begin();
            session.joinTransaction();
            Item item = new Item("Foo");
            session.persist(item);
            tx.commit();
            session.clear();

            tx.begin();
            session.joinTransaction();
            List<AuditLogRecord> logs = session.createQuery(
                    "select lr from AuditLogRecord lr",
                    AuditLogRecord.class
            ).getResultList();
            assertEquals(logs.size(), 1);
            assertEquals(logs.get(0).getMessage(), "insert");
            assertEquals(logs.get(0).getEntityClass(), Item.class);
            assertEquals(logs.get(0).getEntityId(), item.getId());
            assertEquals(logs.get(0).getUserId(), CURRENT_USER_ID);
            session.createQuery("delete AuditLogRecord").executeUpdate();
            tx.commit();
            session.clear();

            tx.begin();
            session.joinTransaction();
            item = session.find(Item.class, item.getId());
            item.setName("Bar");
            tx.commit();
            session.clear();

            tx.begin();
            session.joinTransaction();
            logs = session.createQuery(
                    "select lr from AuditLogRecord lr",
                    AuditLogRecord.class
            ).getResultList();
            assertEquals(logs.size(), 1);
            assertEquals(logs.get(0).getMessage(), "update");
            assertEquals(logs.get(0).getEntityClass(), Item.class);
            assertEquals(logs.get(0).getEntityId(), item.getId());
            assertEquals(logs.get(0).getUserId(), CURRENT_USER_ID);
            tx.commit();
            session.close();

        } finally {
            TM.rollback();
        }
    }

}
