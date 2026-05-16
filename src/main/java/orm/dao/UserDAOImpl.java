package orm.dao;

import models.User;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    final static Logger logger = Logger.getLogger(UserDAOImpl.class);

    @Override
    public User addUser(User user) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            String query = "INSERT INTO User (id, username, password, email, saldo, permissions, level) "
                    + "VALUES (?, ?, PASSWORD(?), ?, ?, ?, ?)";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstm.setInt(1, user.getId());
                pstm.setString(2, user.getUsername());
                pstm.setString(3, user.getPassword());
                pstm.setString(4, user.getEmail());
                pstm.setDouble(5, user.getSaldo());
                pstm.setString(6, user.getPermissions());
                pstm.setInt(7, user.getLevel());
                pstm.executeUpdate();
                copyGeneratedId(user, pstm);
            }
            User saved = getUserByUsername(user.getUsername());
            logger.info("Usuario " + user.getUsername() + " registrado correctamente");
            return saved != null ? saved : user;
        } catch (Exception e) {
            logger.error("No se ha podido registrar el usuario " + user.getUsername(), e);
            throw new RuntimeException("Error al registrar usuario", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User getUser(int id) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            return (User) session.get(User.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User getUserByUsername(String username) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            String query = "SELECT * FROM User WHERE username = ?";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(query)) {
                pstm.setString(1, username);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                            rs.getString("email"), rs.getDouble("saldo"), rs.getString("permissions"),
                            rs.getInt("level"));
                }
            }
        } catch (Exception e) {
            logger.error("Error al buscar usuario " + username, e);
            throw new RuntimeException("Error al buscar usuario", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User getUserByCredentials(String username, String password) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            String query = "SELECT * FROM User WHERE username = ? AND password = PASSWORD(?)";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(query)) {
                pstm.setString(1, username);
                pstm.setString(2, password);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                            rs.getString("email"), rs.getDouble("saldo"), rs.getString("permissions"),
                            rs.getInt("level"));
                }
            }
        } catch (Exception e) {
            logger.error("Error al validar credenciales del usuario " + username, e);
            throw new RuntimeException("Error al validar credenciales", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateUser(Session session, User user) {
        session.update(user);
    }

    @Override
    public List<User> getUsers() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) (List<?>) session.findAll(User.class, new HashMap<String, Object>());
            return users;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getUsers().isEmpty();
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM User").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando usuarios", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void copyGeneratedId(User user, PreparedStatement pstm) throws Exception {
        if (user.getId() > 0) {
            return;
        }
        try (ResultSet keys = pstm.getGeneratedKeys()) {
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
        }
    }
}
