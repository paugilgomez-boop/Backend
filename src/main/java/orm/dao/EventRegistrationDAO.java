package orm.dao;

import models.EventRegistration;

import java.util.List;

public interface EventRegistrationDAO {

    EventRegistration addRegistration(EventRegistration registration);

    List<EventRegistration> getRegistrationsByEvent(int eventId);

    List<EventRegistration> getRegistrationsByUser(int userId);

    boolean existsRegistration(int eventId, int userId);

    void clear();
}