package orm.dao;

import models.EventRegistration;
import orm.FactorySession;
import orm.Session;

import java.util.HashMap;
import java.util.List;

public class EventRegistrationDAOImpl implements EventRegistrationDAO {

    @Override
    public EventRegistration addRegistration(EventRegistration registration) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(registration);
            return registration;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<EventRegistration> getRegistrationsByEvent(int eventId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("eventId", eventId);

            @SuppressWarnings("unchecked")
            List<EventRegistration> registrations =
                    (List<EventRegistration>) (List<?>) session.findAll(EventRegistration.class, params);

            return registrations;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<EventRegistration> getRegistrationsByUser(int userId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            @SuppressWarnings("unchecked")
            List<EventRegistration> registrations =
                    (List<EventRegistration>) (List<?>) session.findAll(EventRegistration.class, params);

            return registrations;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean existsRegistration(int eventId, int userId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("eventId", eventId);
            params.put("userId", userId);

            @SuppressWarnings("unchecked")
            List<EventRegistration> registrations =
                    (List<EventRegistration>) (List<?>) session.findAll(EventRegistration.class, params);

            return !registrations.isEmpty();
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
            session.getConnection().prepareStatement("DELETE FROM EventRegistration").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando inscripciones de eventos", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
