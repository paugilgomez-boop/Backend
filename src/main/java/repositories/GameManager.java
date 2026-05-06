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

    Item updateItem(int itemId, Item item);

    void deleteItem(int itemId);

    List<Item> getAllItems();

    Item getItem(int itemId);

    Purchase buyItem(int userId, int itemId, int quantity);

    List<Inventory> getInventoryByUser(int userId);

    List<Purchase> getPurchasesByUser(int userId);

    User getUser(int userId);

    List<User> getAllUsers();

    void clear();
}
