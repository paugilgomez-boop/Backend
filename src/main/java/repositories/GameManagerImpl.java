package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class GameManagerImpl implements GameManager {

    private static GameManagerImpl instance;
    final static Logger logger = Logger.getLogger(GameManagerImpl.class);

    private HashMap<String, User> users;
    private HashMap<String, User> usersByUsername;
    private HashMap<String, Item> items;
    private HashMap<String, List<Inventory>> inventories;
    private HashMap<String, List<Purchase>> purchases;

    private GameManagerImpl() {
        users = new HashMap<>();
        usersByUsername = new HashMap<>();
        items = new HashMap<>();
        inventories = new HashMap<>();
        purchases = new HashMap<>();
    }

    public static synchronized GameManagerImpl getInstance() {
        if (instance == null) {
            instance = new GameManagerImpl();
            instance.addInitialData();
        }
        return instance;
    }

    private void addInitialData() {
        addItem(new Item("I101", "Espada de madera", "Una espada basica", "WEAPON", 10.0, true, "wood_sword"));
        addItem(new Item("I102", "Escudo de cuero", "Proteccion ligera", "ARMOR", 15.0, true, "leather_shield"));
        addItem(new Item("I103", "Pocion de vida", "Restaura 50 HP", "CONSUMABLE", 5.0, true, "health_potion"));

        registerUser(new User("U101", "admin", "admin", "admin@tower.com", 1000.0, "ADMIN", 10));
        registerUser(new User("U102", "user1", "user1", "user@mail.com", 50.0, "PLAYER", 1));
    }

    @Override
    public void clear() {
        logger.info("clear()");
        users.clear();
        usersByUsername.clear();
        items.clear();
        inventories.clear();
        purchases.clear();
        logger.info("clear completed");
    }

    @Override
    public User registerUser(User user) {
        validateUser(user);
        if (users.containsKey(user.getId()) || usersByUsername.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese id o username");
        }
        users.put(user.getId(), user);
        usersByUsername.put(user.getUsername(), user);
        inventories.put(user.getId(), new ArrayList<Inventory>());
        purchases.put(user.getId(), new ArrayList<Purchase>());
        return user;
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
    public Purchase buyItem(String userId, String itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
        Item item = getItem(itemId);
        if (!item.isAvailable()) {
            throw new IllegalStateException("Item no disponible");
        }
        double totalPrice = item.getPrice() * quantity;
        if (user.getSaldo() < totalPrice) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        user.setSaldo(user.getSaldo() - totalPrice);
        List<Inventory> userInventory = inventories.get(userId);
        if (userInventory == null) {
            userInventory = new ArrayList<>();
            inventories.put(userId, userInventory);
        }

        for (Inventory inventory : userInventory) {
            if (inventory.getItemId().equals(itemId)) {
                inventory.setQuantity(inventory.getQuantity() + quantity);
                Purchase purchase = createPurchase(userId, itemId, quantity, totalPrice, user.getSaldo());
                addPurchase(userId, purchase);
                return purchase;
            }
        }
        Inventory inventory = new Inventory(userId, itemId, quantity);
        userInventory.add(inventory);

        Purchase purchase = createPurchase(userId, itemId, quantity, totalPrice, user.getSaldo());
        addPurchase(userId, purchase);
        return purchase;
    }

    @Override
    public List<Inventory> getInventoryByUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
        List<Inventory> userInventory = inventories.get(userId);
        if (userInventory == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userInventory);
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
        List<Purchase> userPurchases = purchases.get(userId);
        if (userPurchases == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userPurchases);
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
        if (user == null || isBlank(user.getId()) || isBlank(user.getUsername()) || isBlank(user.getPassword()) || isBlank(user.getPermissions())) {
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

    private Purchase createPurchase(String userId, String itemId, int quantity, double totalPrice, double userSaldo) {
        return new Purchase(UUID.randomUUID().toString(), userId, itemId, quantity, totalPrice, userSaldo, Instant.now().toString());
    }

    private void addPurchase(String userId, Purchase purchase) {
        List<Purchase> userPurchases = purchases.get(userId);
        if (userPurchases == null) {
            userPurchases = new ArrayList<>();
            purchases.put(userId, userPurchases);
        }
        userPurchases.add(purchase);
    }
}
