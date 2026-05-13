package orm.dao;

import models.Inventory;
import orm.FactorySession;
import orm.Session;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

public class InventoryDAOImpl implements InventoryDAO {

    @Override
    public void addOrIncreaseItem(Session session, int userId, int itemId, int quantity) {
        try {
            String query = "INSERT INTO Inventory (userId, itemId, quantity) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(query)) {
                pstm.setInt(1, userId);
                pstm.setInt(2, itemId);
                pstm.setInt(3, quantity);
                pstm.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando inventario", e);
        }
    }
    @Override
    public void decreaseOrRemoveItem(Session session, int userId, int itemId, int quantity) {
        try {
            String queryUpdate = "UPDATE Inventory SET quantity = quantity - ? WHERE userId = ? AND itemId = ?";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(queryUpdate)) {
                pstm.setInt(1, quantity);
                pstm.setInt(2, userId);
                pstm.setInt(3, itemId);
                pstm.executeUpdate();
            }
            String queryDelete = "DELETE FROM Inventory WHERE userId = ? AND itemId = ? AND quantity <= 0";
            try (PreparedStatement pstm = session.getConnection().prepareStatement(queryDelete)) {
                pstm.setInt(1, userId);
                pstm.setInt(2, itemId);
                pstm.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al retirar item del inventario", e);
        }
    }

    @Override
    public List<Inventory> getInventoryByUser(int userId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            @SuppressWarnings("unchecked")
            List<Inventory> inventory = (List<Inventory>) (List<?>) session.findAll(Inventory.class, params);
            return inventory;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM Inventory").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando inventario", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
