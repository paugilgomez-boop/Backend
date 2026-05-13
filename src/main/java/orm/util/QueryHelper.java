package orm.util;

import java.util.HashMap;

public class QueryHelper {

    public static String createQueryINSERT(Object entity) {
        String[] fields = ObjectHelper.getFields(entity);
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(entity.getClass().getSimpleName()).append(" (");
        appendFields(sb, fields);
        sb.append(") VALUES (");
        appendPlaceholders(sb, fields.length);
        sb.append(")");
        return sb.toString();
    }

    public static String createQuerySELECT(Class theClass) {
        Object entity = newInstance(theClass);
        String idField = ObjectHelper.getFields(entity)[0];
        return "SELECT * FROM " + theClass.getSimpleName() + " WHERE " + idField + " = ?";
    }

    public static String createQueryUPDATE(Object entity) {
        String[] fields = ObjectHelper.getFields(entity);
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(entity.getClass().getSimpleName()).append(" SET ");
        for (int i = 1; i < fields.length; i++) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(fields[i]).append(" = ?");
        }
        sb.append(" WHERE ").append(fields[0]).append(" = ?");
        return sb.toString();
    }

    public static String createQueryDELETE(Object entity) {
        String idField = ObjectHelper.getFields(entity)[0];
        return "DELETE FROM " + entity.getClass().getSimpleName() + " WHERE " + idField + " = ?";
    }

    public static String createQueryFINDALL(Class theClass, HashMap<String, Object> params) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(theClass.getSimpleName()).append(" WHERE 1=1");
        if (params != null) {
            for (String key : params.keySet()) {
                sb.append(" AND ").append(key).append(" = ?");
            }
        }
        return sb.toString();
    }

    private static Object newInstance(Class theClass) {
        try {
            return theClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se puede instanciar " + theClass.getSimpleName(), e);
        }
    }

    private static void appendFields(StringBuilder sb, String[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(fields[i]);
        }
    }

    private static void appendPlaceholders(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
    }
}
