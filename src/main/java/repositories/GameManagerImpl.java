package repositories;

import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import models.GameEvent;
import models.EventRegistration;
import models.TeamMember;
import models.TeamResponse;
import models.Team;
import models.TeamInfoResponse;
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
import orm.dao.TeamMemberDAO;
import orm.dao.TeamMemberDAOImpl;
import orm.dao.UserDAO;
import orm.dao.UserDAOImpl;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
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
    private final EventDAO eventDAO;
    private final EventRegistrationDAO eventRegistrationDAO;
    private final TeamMemberDAO teamMemberDAO;

    private GameManagerImpl() {
        this.userDAO = new UserDAOImpl();
        this.itemDAO = new ItemDAOImpl();
        this.inventoryDAO = new InventoryDAOImpl();
        this.purchaseDAO = new PurchaseDAOImpl();
        this.eventDAO = new EventDAOImpl();
        this.eventRegistrationDAO = new EventRegistrationDAOImpl();
        this.teamMemberDAO = new TeamMemberDAOImpl();
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
        teamMemberDAO.clear();
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

    @Override
    public List<User> getUsersByEvent(int eventId) {
        getEvent(eventId);

        List<EventRegistration> registrations = eventRegistrationDAO.getRegistrationsByEvent(eventId);
        List<User> users = new ArrayList<>();

        for (EventRegistration registration : registrations) {
            User user = userDAO.getUser(registration.getUserId());
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

    @Override
    public TeamResponse getUserTeam(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username invalido");
        }

        TeamMember member = teamMemberDAO.getMemberByName(username);
        String team = (member != null) ? member.getTeam() : "porxinos";
        List<TeamMember> members = teamMemberDAO.getMembersByTeam(team);
        return new TeamResponse(team, members);
    }

    @Override
    public List<Team> getTeamsRanking() {
        List<TeamMember> allMembers = teamMemberDAO.getAllMembers();
        java.util.Map<String, Team> teamMap = new java.util.HashMap<>();
        
        for (TeamMember m : allMembers) {
            String teamName = m.getTeam();
            if (isBlank(teamName)) continue;
            
            Team team = teamMap.get(teamName);
            if (team == null) {
                team = new Team(teamName, m.getAvatar(), 0);
                teamMap.put(teamName, team);
            }
            team.setPoints(team.getPoints() + m.getPoints());
        }
        
        // Ensure default teams are present in ranking
        String[] defaultTeams = {"porxinos", "aniquiladores", "saiyans", "troncos"};
        for (String tName : defaultTeams) {
            if (!teamMap.containsKey(tName)) {
                teamMap.put(tName, new Team(tName, "https://cdn.pixabay.com/photo/2016/03/31/19/58/avatar-1295397_1280.png", 0));
            }
        }
        
        List<Team> ranking = new java.util.ArrayList<>(teamMap.values());
        ranking.sort((t1, t2) -> Integer.compare(t2.getPoints(), t1.getPoints()));
        return ranking;
    }

    @Override
    public Team joinTeam(String teamName, String username) {
        if (isBlank(teamName) || isBlank(username)) {
            throw new IllegalArgumentException("Parametros invalidos");
        }
        
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new java.util.NoSuchElementException("No existe el usuario " + username);
        }
        
        TeamMember member = teamMemberDAO.getMemberByName(username);
        if (member != null) {
            Session session = null;
            try {
                session = FactorySession.openSession();
                session.beginTransaction();
                TeamMember dbMember = (TeamMember) session.get(TeamMember.class, member.getId());
                if (dbMember != null) {
                    dbMember.setTeam(teamName);
                    session.update(dbMember);
                } else {
                    session.update(member);
                }
                session.commit();
            } catch (RuntimeException e) {
                if (session != null) session.rollback();
                throw e;
            } finally {
                if (session != null) session.close();
            }
        } else {
            String avatar = "https://cdn.pixabay.com/photo/2016/03/31/19/58/avatar-1295397_1280.png";
            TeamMember newMember = new TeamMember(0, teamName, username, avatar, 0);
            teamMemberDAO.addTeamMember(newMember);
        }
        
        List<Team> ranking = getTeamsRanking();
        for (Team t : ranking) {
            if (t.getName().equals(teamName)) {
                return t;
            }
        }
        return new Team(teamName, "https://cdn.pixabay.com/photo/2016/03/31/19/58/avatar-1295397_1280.png", 0);
    }

    @Override
    public TeamInfoResponse getMyTeamInfo(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username invalido");
        }
        
        TeamMember member = teamMemberDAO.getMemberByName(username);
        if (member == null) {
            throw new java.util.NoSuchElementException("El usuario no pertenece a ningun equipo");
        }
        
        String teamName = member.getTeam();
        List<TeamMember> members = teamMemberDAO.getMembersByTeam(teamName);
        return new TeamInfoResponse(teamName, members);
    }

    @Override
    public void leaveTeam(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username invalido");
        }
        
        TeamMember member = teamMemberDAO.getMemberByName(username);
        if (member == null) {
            throw new java.util.NoSuchElementException("El usuario no pertenece a ningun equipo");
        }
        
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.beginTransaction();
            TeamMember dbMember = (TeamMember) session.get(TeamMember.class, member.getId());
            if (dbMember != null) {
                session.delete(dbMember);
            }
            session.commit();
        } catch (RuntimeException e) {
            if (session != null) session.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }


    private void addInitialDataIfNeeded() {
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

        if (teamMemberDAO.isEmpty()) {
            teamMemberDAO.addTeamMember(new TeamMember(
                    1,
                    "porxinos",
                    "Juan",
                    "https://cdn.pixabay.com/photo/2017/07/11/15/51/kermit-2499379_1280.png",
                    250
            ));
            teamMemberDAO.addTeamMember(new TeamMember(
                    2,
                    "porxinos",
                    "Palomo",
                    "https://cdn.pixabay.com/photo/2016/03/31/19/58/avatar-1295397_1280.png",
                    200
            ));
            teamMemberDAO.addTeamMember(new TeamMember(
                    3,
                    "porxinos",
                    "Sergi",
                    "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg",
                    150
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
