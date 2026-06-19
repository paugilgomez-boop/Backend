package models;

public class TeamMember {
    private int id;
    private String team;
    private String name;
    private String avatar;
    private int points;

    public TeamMember() {
    }

    public TeamMember(String name, String avatar, int points) {
        this(0, "porxinos", name, avatar, points);
    }

    public TeamMember(int id, String team, String name, String avatar, int points) {
        this.id = id;
        this.team = team;
        this.name = name;
        this.avatar = avatar;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = username;
    }
}