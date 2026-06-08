package models;

public class EventRegistration {

    private int id;
    private int eventId;
    private int userId;
    private String username;
    private String date;

    public EventRegistration() {
    }

    public EventRegistration(int id, int eventId, int userId, String username, String date) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.username = username;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDate(String date) {
        this.date = date;
    }
}