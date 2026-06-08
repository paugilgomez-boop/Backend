package orm.dao;

import models.GameEvent;

import java.util.List;

public interface EventDAO {

    GameEvent addEvent(GameEvent event);

    GameEvent getEvent(int id);

    List<GameEvent> getEvents();

    boolean isEmpty();

    void clear();
}