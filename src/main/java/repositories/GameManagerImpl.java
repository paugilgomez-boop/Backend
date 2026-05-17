package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import orm.FactorySession;
import orm.Session;
import orm.dao.InventoryDAO;
import orm.dao.InventoryDAOImpl;
import orm.dao.ItemDAO;
import orm.dao.ItemDAOImpl;
import orm.dao.PurchaseDAO;
import orm.dao.PurchaseDAOImpl;
import orm.dao.UserDAO;
import orm.dao.UserDAOImpl;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class GameManagerImpl implements GameManager {

    private static GameManagerImpl instance;
    final static Logger logger = Logger.getLogger(GameManagerImpl.class);

    private final UserDAO userDAO;
    private final ItemDAO itemDAO;
    private final InventoryDAO inventoryDAO;
    private final PurchaseDAO purchaseDAO;

    private GameManagerImpl() {
        this.userDAO = new UserDAOImpl();
        this.itemDAO = new ItemDAOImpl();
        this.inventoryDAO = new InventoryDAOImpl();
        this.purchaseDAO = new PurchaseDAOImpl();
        addInitialDataIfNeeded();
    }

    public static synchronized GameManagerImpl getInstance() {
        if (instance == null) {
            instance = new GameManagerImpl();
        }
        return instance;
    }

    @Override
    public void clear() {
        logger.info("clear database data");
        purchaseDAO.clear();
        inventoryDAO.clear();
        itemDAO.clear();
        userDAO.clear();
        logger.info("clear completed");
    }

    @Override
    public User registerUser(User user) {
        validateUser(user);
        if (user.getId() > 0 && userDAO.getUser(user.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese id");
        }
        if (userDAO.getUserByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese username");
        }
        return userDAO.addUser(user);
    }

    @Override
    public User login(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new NoSuchElementException("Credenciales invalidas");
        }
        return user;
    }

    @Override
    public Item addItem(Item item) {
        validateItem(item);
        if (item.getId() > 0 && itemDAO.getItem(item.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un item con ese id");
        }
        return itemDAO.addItem(item);
    }

    @Override
    public Item updateItem(int itemId, Item item) {
        if (itemDAO.getItem(itemId) == null) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        validateItem(item);
        return itemDAO.updateItem(itemId, item);
    }

    @Override
    public void deleteItem(int itemId) {
        Item item = itemDAO.getItem(itemId);
        if (item == null) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        itemDAO.deleteItem(item);
    }

    @Override
    public List<Item> getAllItems() {
        return itemDAO.getItems();
    }

    @Override
    public Item getItem(int itemId) {
        Item item = itemDAO.getItem(itemId);
        if (item == null) {
            throw new NoSuchElementException("No existe ningun item con ese id");
        }
        return item;
    }

    @Override
    public Purchase buyItem(int userId, int itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        Session session = null;
        try {
            session = FactorySession.openSession();
            session.beginTransaction();

            User user = (User) session.get(User.class, userId);
            if (user == null) {
                throw new NoSuchElementException("No existe ningun usuario con ese id");
            }

            Item item = (Item) session.get(Item.class, itemId);
            if (item == null) {
                throw new NoSuchElementException("No existe ningun item con ese id");
            }
            if (!item.isAvailable()) {
                throw new IllegalStateException("Item no disponible");
            }

            double totalPrice = item.getPrice() * quantity;
            if (user.getSaldo() < totalPrice) {
                throw new IllegalStateException("Saldo insuficiente");
            }

            double newSaldo = user.getSaldo() - totalPrice;
            user.setSaldo(newSaldo);
            userDAO.updateUser(session, user);
            inventoryDAO.addOrIncreaseItem(session, userId, itemId, quantity);

            Purchase purchase = new Purchase(0, userId, itemId, quantity, totalPrice, newSaldo, Instant.now().toString());
            purchaseDAO.addPurchase(session, purchase);

            session.commit();
            return purchase;
        } catch (RuntimeException e) {
            if (session != null) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Purchase sellItem(int userId, int itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        Session session = null;
        try {
            session = FactorySession.openSession();
            session.beginTransaction();

            User user = (User) session.get(User.class, userId);
            if (user == null) {
                throw new NoSuchElementException("No existe ningun usuario con ese id");
            }

            Item item = (Item) session.get(Item.class, itemId);
            if (item == null) {
                throw new NoSuchElementException("No existe ningun item con ese id");
            }

            // Check if user has the item and enough quantity
            HashMap<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("itemId", itemId);
            @SuppressWarnings("unchecked")
            List<Inventory> invList = (List<Inventory>) (List<?>) session.findAll(Inventory.class, params);
            if (invList.isEmpty() || invList.get(0).getQuantity() < quantity) {
                throw new IllegalStateException("No tienes suficiente cantidad de este item");
            }

            double refund = item.getPrice() * quantity;
            double newSaldo = user.getSaldo() + refund;
            
            user.setSaldo(newSaldo);
            userDAO.updateUser(session, user);
            inventoryDAO.decreaseOrRemoveItem(session, userId, itemId, quantity);

            Purchase saleRecord = new Purchase(0, userId, itemId, -quantity, -refund, newSaldo, java.time.Instant.now().toString());
            purchaseDAO.addPurchase(session, saleRecord);

            session.commit();
            return saleRecord;
        } catch (RuntimeException e) {
            if (session != null) {
                session.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Inventory> getInventoryByUser(int userId) {
        ensureUserExists(userId);
        return inventoryDAO.getInventoryByUser(userId);
    }

    @Override
    public List<Purchase> getPurchasesByUser(int userId) {
        ensureUserExists(userId);
        return purchaseDAO.getPurchasesByUser(userId);
    }

    @Override
    public User getUser(int userId) {
        User user = userDAO.getUser(userId);
        if (user == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.getUsers();
    }

    private void addInitialDataIfNeeded() {
        // Comprobar si la base de datos tiene objetos de la tienda antigua
        boolean hasOldItems = false;
        try {
            for (Item item : itemDAO.getItems()) {
                if ("Espada de madera".equalsIgnoreCase(item.getName()) || 
                    "Escudo de cuero".equalsIgnoreCase(item.getName()) || 
                    "Pocion de vida".equalsIgnoreCase(item.getName())) {
                    hasOldItems = true;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error al comprobar items antiguos", e);
        }

        // Si hay objetos antiguos, limpiamos la base de datos automáticamente
        if (hasOldItems) {
            logger.info("Detectados objetos de la tienda antigua. Limpiando base de datos para actualizar...");
            clear();
        }

        if (itemDAO.isEmpty()) {
            addItem(new Item(1, "Refuerzo de Muralla", "Aumenta la vida de la base", "ARMOR", 100.0, true, "wall_upgrade.png"));
            addItem(new Item(2, "Calibracion de Cañon", "Mejora el daño de disparo", "WEAPON", 150.0, true, "damage_upgrade.png"));
            addItem(new Item(3, "Municion de Cadencia", "Aumenta la velocidad de ataque", "BOOST", 120.0, true, "speed_upgrade.png"));
            addItem(new Item(4, "Lente de Precision", "Aumenta el rango de ataque", "BOOST", 200.0, true, "range_upgrade.png"));
            addItem(new Item(5, "Kit de Reparacion", "Restaura vida de la base", "CONSUMABLE", 50.0, true, "repair_kit.png"));
        }

        if (userDAO.isEmpty()) {
            registerUser(new User(101, "admin", "admin", "admin@tower.com", 1000.0, "ADMIN", 10));
            registerUser(new User(102, "user1", "user1", "user@mail.com", 50.0, "PLAYER", 1));
        }
    }

    private void ensureUserExists(int userId) {
        if (userDAO.getUser(userId) == null) {
            throw new NoSuchElementException("No existe ningun usuario con ese id");
        }
    }

    private void validateUser(User user) {
        if (user == null || isBlank(user.getUsername()) || isBlank(user.getPassword()) || isBlank(user.getPermissions())) {
            throw new IllegalArgumentException("Datos de usuario invalidos");
        }
    }

    private void validateItem(Item item) {
        if (item == null || isBlank(item.getName()) || isBlank(item.getType()) || item.getPrice() < 0) {
            throw new IllegalArgumentException("Datos de item invalidos");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
