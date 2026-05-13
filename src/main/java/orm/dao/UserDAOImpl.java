package orm.dao;

import models.User;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    final static Logger logger = Logger.getLogger(UserDAOImpl.class);

    @Override
    public User addUser(User user) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(user);
            User saved = getUserByUsername(user.getUsername());
            logger.info("Usuario " + user.getUsername() + " registrado correctamente");
            return saved != null ? saved : user;
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
}
