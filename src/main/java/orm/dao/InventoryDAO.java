package orm.dao;

import models.Inventory;
import orm.Session;

import java.util.List;

public interface InventoryDAO {
    void addOrIncreaseItem(Session session, int userId, int itemId, int quantity);

    void decreaseOrRemoveItem(Session session, int userId, int itemId, int quantity);

    List<Inventory> getInventoryByUser(int userId);

    void clear();
}
