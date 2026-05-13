package orm.dao;

import models.Purchase;
import orm.FactorySession;
import orm.Session;

import java.util.HashMap;
import java.util.List;

public class PurchaseDAOImpl implements PurchaseDAO {

    @Override
    public Purchase addPurchase(Session session, Purchase purchase) {
        session.save(purchase);
        return purchase;
    }

    @Override
    public List<Purchase> getPurchasesByUser(int userId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            @SuppressWarnings("unchecked")
            List<Purchase> purchases = (List<Purchase>) (List<?>) session.findAll(Purchase.class, params);
            return purchases;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM Purchase").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando compras", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
