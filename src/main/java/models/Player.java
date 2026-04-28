package models;

public class Player extends User {
    private int level;

    public Player() {
    }

    public Player(String id, String username, String password, String email, double saldo, int level) {
        super(id, username, password, email, saldo);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
