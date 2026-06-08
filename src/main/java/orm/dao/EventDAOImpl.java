package orm.dao;

import models.GameEvent;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class EventDAOImpl implements EventDAO {

    final static Logger logger = Logger.getLogger(EventDAOImpl.class);

    @Override
    public GameEvent addEvent(GameEvent event) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(event);
            logger.info("Event " + event.getName() + " guardado correctamente");
            return event.getId() > 0 ? getEvent(event.getId()) : event;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public GameEvent getEvent(int id) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            return (GameEvent) session.get(GameEvent.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<GameEvent> getEvents() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            @SuppressWarnings("unchecked")
            List<GameEvent> events = (List<GameEvent>) (List<?>) session.findAll(GameEvent.class, new HashMap<String, Object>());
            return events;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getEvents().isEmpty();
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM GameEvent").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando eventos", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}