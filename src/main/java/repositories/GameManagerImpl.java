package repositories;

import models.Admin;
import models.Inventory;
import models.Item;
import models.Player;
import models.User;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class GameManagerImpl implements GameManager {

    private static GameManagerImpl instance;
    final static Logger logger = Logger.getLogger(GameManagerImpl.class);

    private HashMap<String, User> users;
    private HashMap<String, User> usersByUsername;
    private HashMap<String, Item> items;
    private HashMap<String, List<Inventory>> inventories;

    private GameManagerImpl() {
        users = new HashMap<>();
        usersByUsername = new HashMap<>();
        items = new HashMap<>();
        inventories = new HashMap<>();
    }

    public static GameManagerImpl getInstance() {
        logger.info("getInstance()");
        if (instance == null) {
            instance = new GameManagerImpl();
            logger.info("new repositories.GameManagerImpl()");
        }
        logger.info("getInstance completed");
        return instance;
    }

    @Override
    public void clear() {
        logger.info("clear()");
        users.clear();
        usersByUsername.clear();
        items.clear();
        inventories.clear();
        logger.info("clear completed");
    }

    @Override
    public Player registerPlayer(Player player) {
        validateUser(player);
        if (users.containsKey(player.getId()) || usersByUsername.containsKey(player.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese id o username");
        }
        users.put(player.getId(), player);
        usersByUsername.put(player.getUsername(), player);
        inventories.put(player.getId(), new ArrayList<Inventory>());
        return player;
    }

    @Override
    public Admin registerAdmin(Admin admin) {
        validateUser(admin);
        if (users.containsKey(admin.getId()) || usersByUsername.containsKey(admin.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese id o username");
        }
        users.put(admin.getId(), admin);
        usersByUsername.put(admin.getUsername(), admin);
        return admin;
    }

    @Override
    public User login(String username, String password) {
        User user = usersByUsername.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new NoSuchElementException("Credenciales invalidas");
        }
        return user;
    }

    @Override
    public Item addItem(Item item) {
        validateItem(item);
        if (items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Ya existe un item con ese id");
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(String itemId, Item item) {
        if (!items.containsKey(itemId)) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        if (item != null) {
            item.setId(itemId);
        }
        validateItem(item);
        items.put(itemId, item);
        updateInventoryItemType(itemId, item.getType());
        return item;
    }

    @Override
    public void deleteItem(String itemId) {
        if (!items.containsKey(itemId)) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        items.remove(itemId);
        removeItemFromInventories(itemId);
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item getItem(String itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        return item;
    }

    @Override
    public Inventory buyItem(String playerId, String itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        User user = users.get(playerId);
        if (!(user instanceof Player)) {
            throw new NoSuchElementException("No existe ningun player con ese id");
        }
        Item item = getItem(itemId);
        double totalPrice = item.getPrice() * quantity;
        if (user.getSaldo() < totalPrice) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        user.setSaldo(user.getSaldo() - totalPrice);
        List<Inventory> playerInventory = inventories.get(playerId);
        if (playerInventory == null) {
            playerInventory = new ArrayList<>();
            inventories.put(playerId, playerInventory);
        }

        for (Inventory inventory : playerInventory) {
            if (inventory.getItemId().equals(itemId)) {
                inventory.setQuantity(inventory.getQuantity() + quantity);
                return inventory;
            }
        }

        Inventory inventory = new Inventory(playerId, itemId, item.getType(), quantity);
        playerInventory.add(inventory);
        return inventory;
    }

    @Override
    public List<Inventory> getInventoryByPlayer(String playerId) {
        User user = users.get(playerId);
        if (!(user instanceof Player)) {
            throw new NoSuchElementException("No existe ningun player con ese id");
        }
        List<Inventory> playerInventory = inventories.get(playerId);
        if (playerInventory == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(playerInventory);
    }

    @Override
    public User getUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
        return user;
    }

    private void validateUser(User user) {
        if (user == null || isBlank(user.getId()) || isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            throw new IllegalArgumentException("Datos de usuario invalidos");
        }
    }

    private void validateItem(Item item) {
        if (item == null || isBlank(item.getId()) || isBlank(item.getName()) || isBlank(item.getType()) || item.getPrice() < 0) {
            throw new IllegalArgumentException("Datos de item invalidos");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void updateInventoryItemType(String itemId, String type) {
        for (List<Inventory> playerInventory : inventories.values()) {
            for (Inventory inventory : playerInventory) {
                if (inventory.getItemId().equals(itemId)) {
                    inventory.setType(type);
                }
            }
        }
    }

    private void removeItemFromInventories(String itemId) {
        for (List<Inventory> playerInventory : inventories.values()) {
            Iterator<Inventory> iterator = playerInventory.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getItemId().equals(itemId)) {
                    iterator.remove();
                }
            }
        }
    }
}
