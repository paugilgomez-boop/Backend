package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;

import java.util.List;

public interface GameManager {

    User registerUser(User user);

    User login(String username, String password);

    Item addItem(Item item);

    Item updateItem(String itemId, Item item);

    void deleteItem(String itemId);

    List<Item> getAllItems();

    Item getItem(String itemId);

    Purchase buyItem(String userId, String itemId, int quantity);

    List<Inventory> getInventoryByUser(String userId);

    List<Purchase> getPurchasesByUser(String userId);

    User getUser(String userId);

    void clear();
}
