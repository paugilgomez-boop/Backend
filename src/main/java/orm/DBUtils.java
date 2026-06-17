package orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    public static final String DB_NAME = config("db.name", "DB_NAME", "towerdefence");
    public static final String DB_HOST = config("db.host", "DB_HOST", "127.0.0.1");
    public static final String DB_USER = config("db.user", "DB_USER", "root");
    public static final String DB_PASS = config("db.password", "DB_PASS", "pgilDSA");
    public static final String DB_PORT = config("db.port", "DB_PORT", "3306");

    private static String config(String systemProperty, String envVar, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isEmpty()) {
            value = System.getenv(envVar);
        }
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public static String getDb() {
        return DB_NAME;
    }

    public static String getDbHost() {
        return DB_HOST;
    }

    public static String getDbUser() {
        return DB_USER;
    }

    public static String getDbPasswd() {
        return DB_PASS;
    }

    public static String getDbPort() {
        return DB_PORT;
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                + "?connectTimeout=2000&socketTimeout=5000";
        return DriverManager.getConnection(url, DB_USER, DB_PASS);
    }
}
