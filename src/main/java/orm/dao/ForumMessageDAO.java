package orm.dao;

import models.ForumMessage;
import java.util.List;

public interface ForumMessageDAO {
    ForumMessage addMessage(ForumMessage message);
    List<ForumMessage> getMessagesByTopic(int topicId);
    void clear();
}
