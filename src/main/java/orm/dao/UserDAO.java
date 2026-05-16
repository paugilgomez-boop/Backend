package orm.dao;

import models.User;
import orm.Session;

import java.util.List;

public interface UserDAO {
    User addUser(User user);

    User getUser(int id);

    User getUserByUsername(String username);

    User getUserByCredentials(String username, String password);

    void updateUser(Session session, User user);

    List<User> getUsers();

    boolean isEmpty();

    void clear();
}
