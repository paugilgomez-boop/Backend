package orm;

import orm.util.ObjectHelper;
import orm.util.QueryHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionImpl implements Session {
    private final Connection conn;
    private boolean inTransaction;

    public SessionImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Object entity) {
        String query = QueryHelper.createQueryINSERT(entity);
        // logger.info
        try (PreparedStatement pstm = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (String field : ObjectHelper.getFields(entity)) {
                pstm.setObject(index++, ObjectHelper.getter(entity, field));
            }
            pstm.executeUpdate();
            copyGeneratedId(entity, pstm);
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object get(Class theClass, Object id) {
        String query = QueryHelper.createQuerySELECT(theClass);
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            pstm.setObject(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(theClass, rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error buscando " + theClass.getSimpleName(), e);
        }
    }

    @Override
    public void update(Object entity) {
        String query = QueryHelper.createQueryUPDATE(entity);
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            String[] fields = ObjectHelper.getFields(entity);
            int index = 1;
            for (int i = 1; i < fields.length; i++) {
                pstm.setObject(index++, ObjectHelper.getter(entity, fields[i]));
            }
            pstm.setObject(index, ObjectHelper.getter(entity, fields[0]));
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void delete(Object entity) {
        String query = QueryHelper.createQueryDELETE(entity);
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            String idField = ObjectHelper.getFields(entity)[0];
            pstm.setObject(1, ObjectHelper.getter(entity, idField));
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public List<Object> findAll(Class theClass, HashMap<String, Object> params) {
        String query = QueryHelper.createQueryFINDALL(theClass, params);
        List<Object> result = new ArrayList<>();
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            int index = 1;
            if (params != null) {
                for (String key : params.keySet()) {
                    pstm.setObject(index++, params.get(key));
                }
            }
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(theClass, rs));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error listando " + theClass.getSimpleName(), e);
        }
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void beginTransaction() {
        try {
            conn.setAutoCommit(false);
            inTransaction = true;
        } catch (SQLException e) {
            throw new RuntimeException("Error iniciando transaccion", e);
        }
    }

    @Override
    public void commit() {
        try {
            if (inTransaction) {
                conn.commit();
                conn.setAutoCommit(true);
                inTransaction = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error confirmando transaccion", e);
        }
    }

    @Override
    public void rollback() {
        try {
            if (inTransaction) {
                conn.rollback();
                conn.setAutoCommit(true);
                inTransaction = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cancelando transaccion", e);
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cerrando sesion", e);
        }
    }

    private Object mapRow(Class theClass, ResultSet rs) throws Exception {
        Object object = theClass.getDeclaredConstructor().newInstance();
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            ObjectHelper.setter(object, metadata.getColumnName(i), rs.getObject(i));
        }
        return object;
    }

    private void copyGeneratedId(Object entity, PreparedStatement pstm) throws SQLException {
        String[] fields = ObjectHelper.getFields(entity);
        if (fields.length == 0 || !"id".equals(fields[0])) {
            return;
        }
        Object currentId = ObjectHelper.getter(entity, "id");
        if (!(currentId instanceof Number) || ((Number) currentId).intValue() > 0) {
            return;
        }
        try (ResultSet keys = pstm.getGeneratedKeys()) {
            if (keys.next()) {
                ObjectHelper.setter(entity, "id", keys.getInt(1));
            }
        }
    }
}
