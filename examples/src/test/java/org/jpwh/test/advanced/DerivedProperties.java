package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Bid;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class DerivedProperties extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadFormula() throws Exception {
        // This test case makes use of database formula features that may not work in all databases.
        // So while we try to make it work, such usage is not recommended: why don't write our domain logic in Java
        // where portability is guaranteed and execution is removed from the db servers.
        long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getShortDescription(), "This is some...");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeLoadFormulaSubselect() throws Exception {
        long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getAverageBidAmount().compareTo(new BigDecimal("12")), 0);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    public Long storeItemAndBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Item item = new Item();
        item.setName("Some item");
        item.setDescription("This is some description.");
        em.persist(item);
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid();
            bid.setAmount(new BigDecimal(10 + i));
            bid.setItem(item);
            em.persist(bid);
        }
        tx.commit();
        em.close();
        return item.getId();
    }

}
