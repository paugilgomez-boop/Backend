package repositories;

import models.Admin;
import models.Inventory;
import models.Item;
import models.Player;
import models.User;

import java.util.List;

public interface GameManager {

    Player registerPlayer(Player player);

    Admin registerAdmin(Admin admin);

    User login(String username, String password);

    Item addItem(Item item);

    Item updateItem(String itemId, Item item);

    void deleteItem(String itemId);

    List<Item> getAllItems();

    Item getItem(String itemId);

    Inventory buyItem(String playerId, String itemId, int quantity);

    List<Inventory> getInventoryByPlayer(String playerId);

    User getUser(String userId);

    void clear();
}
