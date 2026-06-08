package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import models.GameEvent;
import models.EventRegistration;
import orm.FactorySession;
import orm.Session;
import orm.dao.EventDAO;
import orm.dao.EventDAOImpl;
import orm.dao.EventRegistrationDAO;
import orm.dao.EventRegistrationDAOImpl;
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
import java.util.List;
import java.util.NoSuchElementException;

public class GameManagerImpl implements GameManager {

    private static GameManagerImpl instance;
    final static Logger logger = Logger.getLogger(GameManagerImpl.class);

    private final UserDAO userDAO;
    private final ItemDAO itemDAO;
    private final InventoryDAO inventoryDAO;
    private final PurchaseDAO purchaseDAO;
    private final EventDAO eventDAO;
    private final EventRegistrationDAO eventRegistrationDAO;

    private GameManagerImpl() {
        this.userDAO = new UserDAOImpl();
        this.itemDAO = new ItemDAOImpl();
        this.inventoryDAO = new InventoryDAOImpl();
        this.purchaseDAO = new PurchaseDAOImpl();
        this.eventDAO = new EventDAOImpl();
        this.eventRegistrationDAO = new EventRegistrationDAOImpl();
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
        eventRegistrationDAO.clear();
        purchaseDAO.clear();
        inventoryDAO.clear();
        eventDAO.clear();
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
        User user = userDAO.getUserByCredentials(username, password);
        if (user == null) {
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

    @Override
    public List<GameEvent> getEvents() {
        return eventDAO.getEvents();
    }

    @Override
    public GameEvent getEvent(int eventId) {
        GameEvent event = eventDAO.getEvent(eventId);
        if (event == null) {
            throw new NoSuchElementException("No existe ningun evento con ese id");
        }
        return event;
    }

    @Override
    public EventRegistration registerToEvent(int eventId, int userId, String username) {
        ensureUserExists(userId);

        GameEvent event = eventDAO.getEvent(eventId);
        if (event == null) {
            throw new NoSuchElementException("No existe ningun evento con ese id");
        }

        if (isBlank(username)) {
            throw new IllegalArgumentException("Username invalido");
        }

        if (eventRegistrationDAO.existsRegistration(eventId, userId)) {
            throw new IllegalStateException("El usuario ya esta inscrito en este evento");
        }

        EventRegistration registration = new EventRegistration(
                0,
                eventId,
                userId,
                username,
                Instant.now().toString()
        );

        return eventRegistrationDAO.addRegistration(registration);
    }

    private void addInitialDataIfNeeded() {
        if (itemDAO.isEmpty()) {
            addItem(new Item(101, "Espada de madera", "Una espada basica", "WEAPON", 10.0, true, "wood_sword"));
            addItem(new Item(102, "Escudo de cuero", "Proteccion ligera", "ARMOR", 15.0, true, "leather_shield"));
            addItem(new Item(103, "Pocion de vida", "Restaura 50 HP", "CONSUMABLE", 5.0, true, "health_potion"));
        }

        if (userDAO.isEmpty()) {
            registerUser(new User(101, "admin", "admin", "admin@tower.com", 1000.0, "ADMIN", 10));
            registerUser(new User(102, "user1", "user1", "user@mail.com", 50.0, "PLAYER", 1));
        }

        if (eventDAO.isEmpty()) {
            eventDAO.addEvent(new GameEvent(
                    1,
                    "Torneo de Torres",
                    "Compite durante tres dias y consigue puntos extra para tu equipo.",
                    "https://cdn.pixabay.com/photo/2016/11/29/05/08/adventure-1868817_1280.jpg",
                    "2026-06-02",
                    "2026-06-05"
            ));

            eventDAO.addEvent(new GameEvent(
                    2,
                    "Defensa Nocturna",
                    "Evento especial con oleadas mas dificiles y recompensas exclusivas.",
                    "https://cdn.pixabay.com/photo/2016/10/30/05/43/castle-1781648_1280.jpg",
                    "2026-06-06",
                    "2026-06-08"
            ));

            eventDAO.addEvent(new GameEvent(
                    3,
                    "Reto Cooperativo",
                    "Un desafio para jugadores que quieran coordinarse con su equipo.",
                    "https://cdn.pixabay.com/photo/2017/08/30/01/05/fantasy-2696946_1280.jpg",
                    "2026-06-10",
                    "2026-06-12"
            ));
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
