package orm;

import orm.util.ObjectHelper;
import orm.util.QueryHelper;
import org.apache.log4j.Logger;

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
    final static Logger logger = Logger.getLogger(SessionImpl.class);

    private final Connection conn;
    private boolean inTransaction;

    public SessionImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Object entity) {
        String query = QueryHelper.createQueryINSERT(entity);
        logger.info("save " + entity.getClass().getSimpleName());
        try (PreparedStatement pstm = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (String field : ObjectHelper.getFields(entity)) {
                pstm.setObject(index++, ObjectHelper.getter(entity, field));
            }
            pstm.executeUpdate();
            copyGeneratedId(entity, pstm);
            logger.info("save completed for " + entity.getClass().getSimpleName());
        } catch (SQLException e) {
            logger.error("Error guardando " + entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Error guardando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object get(Class theClass, Object id) {
        String query = QueryHelper.createQuerySELECT(theClass);
        logger.info("get " + theClass.getSimpleName() + " id=" + id);
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            pstm.setObject(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (!rs.next()) {
                    logger.info("get without result for " + theClass.getSimpleName() + " id=" + id);
                    return null;
                }
                Object result = mapRow(theClass, rs);
                logger.info("get completed for " + theClass.getSimpleName() + " id=" + id);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error buscando " + theClass.getSimpleName() + " id=" + id, e);
            throw new RuntimeException("Error buscando " + theClass.getSimpleName(), e);
        }
    }

    @Override
    public void update(Object entity) {
        String query = QueryHelper.createQueryUPDATE(entity);
        logger.info("update " + entity.getClass().getSimpleName());
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            String[] fields = ObjectHelper.getFields(entity);
            int index = 1;
            for (int i = 1; i < fields.length; i++) {
                pstm.setObject(index++, ObjectHelper.getter(entity, fields[i]));
            }
            pstm.setObject(index, ObjectHelper.getter(entity, fields[0]));
            pstm.executeUpdate();
            logger.info("update completed for " + entity.getClass().getSimpleName());
        } catch (SQLException e) {
            logger.error("Error actualizando " + entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Error actualizando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void delete(Object entity) {
        String query = QueryHelper.createQueryDELETE(entity);
        logger.info("delete " + entity.getClass().getSimpleName());
        try (PreparedStatement pstm = conn.prepareStatement(query)) {
            String idField = ObjectHelper.getFields(entity)[0];
            pstm.setObject(1, ObjectHelper.getter(entity, idField));
            pstm.executeUpdate();
            logger.info("delete completed for " + entity.getClass().getSimpleName());
        } catch (SQLException e) {
            logger.error("Error eliminando " + entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Error eliminando " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public List<Object> findAll(Class theClass, HashMap<String, Object> params) {
        String query = QueryHelper.createQueryFINDALL(theClass, params);
        List<Object> result = new ArrayList<>();
        logger.info("findAll " + theClass.getSimpleName() + " filters=" + (params == null ? 0 : params.size()));
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
            logger.info("findAll completed for " + theClass.getSimpleName() + " results=" + result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error listando " + theClass.getSimpleName(), e);
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
            logger.info("transaction started");
        } catch (SQLException e) {
            logger.error("Error iniciando transaccion", e);
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
                logger.info("transaction committed");
            }
        } catch (SQLException e) {
            logger.error("Error confirmando transaccion", e);
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
                logger.info("transaction rolled back");
            }
        } catch (SQLException e) {
            logger.error("Error cancelando transaccion", e);
            throw new RuntimeException("Error cancelando transaccion", e);
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.info("session closed");
            }
        } catch (SQLException e) {
            logger.error("Error cerrando sesion", e);
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
                int generatedId = keys.getInt(1);
                ObjectHelper.setter(entity, "id", generatedId);
                logger.info("generated id assigned to " + entity.getClass().getSimpleName() + ": " + generatedId);
            }
        }
    }
}
