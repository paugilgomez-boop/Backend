package orm;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public interface Session {
    void save(Object entity);

    Object get(Class theClass, Object id);

    void update(Object entity);

    void delete(Object entity);

    List<Object> findAll(Class theClass, HashMap<String, Object> params);

    Connection getConnection();

    void beginTransaction();

    void commit();

    void rollback();

    void close();
}
