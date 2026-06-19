package orm.dao;

import models.ForumTopic;
import orm.FactorySession;
import orm.Session;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class ForumTopicDAOImpl implements ForumTopicDAO {

    final static Logger logger = Logger.getLogger(ForumTopicDAOImpl.class);

    @Override
    public ForumTopic addTopic(ForumTopic topic) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.save(topic);
            logger.info("ForumTopic '" + topic.getTitle() + "' guardada correctamente");
            return topic.getId() > 0 ? getTopic(topic.getId()) : topic;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public ForumTopic getTopic(int id) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            return (ForumTopic) session.get(ForumTopic.class, id);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<ForumTopic> getTopics() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            @SuppressWarnings("unchecked")
            List<ForumTopic> topics = (List<ForumTopic>) (List<?>) session.findAll(ForumTopic.class, new HashMap<String, Object>());
            return topics;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateTopic(ForumTopic topic) {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.update(topic);
            logger.info("ForumTopic '" + topic.getTitle() + "' actualizada correctamente");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getTopics().isEmpty();
    }

    @Override
    public void clear() {
        Session session = null;
        try {
            session = FactorySession.openSession();
            session.getConnection().prepareStatement("DELETE FROM ForumTopic").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error borrando tematicas del foro", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
