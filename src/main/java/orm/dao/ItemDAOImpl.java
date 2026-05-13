package orm.dao;

import models.Item;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {
    final static Logger logger = Logger.getLogger(ItemDAOImpl.class);

    @Override
    public Item addItem(Item item) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(item);
            logger.info("Item " + item.getName() + " guardado correctamente");
            return item.getId() > 0 ? getItem(item.getId()) : item;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Item getItem(int id) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            return (Item) session.get(Item.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Item updateItem(int id, Item item) {
        item.setId(id);
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.update(item);
            return getItem(id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void deleteItem(Item item) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.delete(item);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Item> getItems() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            @SuppressWarnings("unchecked")
            List<Item> items = (List<Item>) (List<?>) session.findAll(Item.class, new HashMap<String, Object>());
            return items;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getItems().isEmpty();
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM Item").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando items", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
