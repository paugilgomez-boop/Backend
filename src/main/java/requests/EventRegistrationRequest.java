package requests;

public class EventRegistrationRequest {

    private int userId;
    private String username;

    public EventRegistrationRequest() {
    }

    public EventRegistrationRequest(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}