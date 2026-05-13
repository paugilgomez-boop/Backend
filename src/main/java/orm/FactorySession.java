package orm;

import java.sql.Connection;

public class FactorySession {

    public static Session openSession() {
        try {
            Connection conn = DBUtils.getConnection();
            return new SessionImpl(conn);
        } catch (Exception e) {
            throw new RuntimeException("No se ha podido abrir la sesion con la base de datos", e);
        }
    }
}
