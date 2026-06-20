package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import models.GameEvent;
import models.EventRegistration;
import models.TeamResponse;
import models.Team;
import models.TeamInfoResponse;
import responses.EarnCoinsResponse;
import responses.GameUpgradePurchaseResponse;
import responses.GameUpgradesResponse;

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
    
    Purchase sellItem(int userId, int itemId, int quantity);

    List<Inventory> getInventoryByUser(int userId);

    List<Purchase> getPurchasesByUser(int userId);

    User getUser(int userId);

    List<User> getAllUsers();

    List<GameEvent> getEvents();

    GameEvent getEvent(int eventId);

    EventRegistration registerToEvent(int eventId, int userId, String username);

    List<User> getUsersByEvent(int eventId);

    TeamResponse getUserTeam(String username);

    List<Team> getTeamsRanking();

    Team joinTeam(String teamName, String username);

    TeamInfoResponse getMyTeamInfo(String username);

    void leaveTeam(String username);

    GameUpgradesResponse getUpgradesByUsername(String username);

    GameUpgradePurchaseResponse purchaseUpgrade(String username, String upgradeType);

    EarnCoinsResponse earnCoins(String username, int coinsEarned);

    void clear();
}
