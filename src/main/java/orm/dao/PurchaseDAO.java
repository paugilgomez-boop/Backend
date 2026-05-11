package orm.dao;

import models.Purchase;
import orm.Session;

import java.util.List;

public interface PurchaseDAO {
    Purchase addPurchase(Session session, Purchase purchase);

    List<Purchase> getPurchasesByUser(int userId);

    void clear();
}
