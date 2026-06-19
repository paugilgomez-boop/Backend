package orm.dao;

import models.ForumMessage;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class ForumMessageDAOImpl implements ForumMessageDAO {

    final static Logger logger = Logger.getLogger(ForumMessageDAOImpl.class);

    @Override
    public ForumMessage addMessage(ForumMessage message) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(message);
            logger.info("ForumMessage saved successfully");
            return message.getId() > 0 ? (ForumMessage) session.get(ForumMessage.class, message.getId()) : message;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<ForumMessage> getMessagesByTopic(int topicId) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            HashMap<String, Object> params = new HashMap<>();
            params.put("topicId", topicId);
            @SuppressWarnings("unchecked")
            List<ForumMessage> messages = (List<ForumMessage>) (List<?>) session.findAll(ForumMessage.class, params);
            return messages;
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
            session.getConnection().prepareStatement("DELETE FROM ForumMessage").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error clearing forum messages", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
