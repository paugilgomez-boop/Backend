package orm.dao;

import models.ForumTopic;
import java.util.List;

public interface ForumTopicDAO {
    ForumTopic addTopic(ForumTopic topic);
    ForumTopic getTopic(int id);
    List<ForumTopic> getTopics();
    void updateTopic(ForumTopic topic);
    boolean isEmpty();
    void clear();
}
