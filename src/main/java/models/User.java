package models;

public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private double saldo;
    private String permissions;
    private int level;

    public User() {
    }

    public User(String id, String username, String password, String email, double saldo, String permissions, int level) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.saldo = saldo;
        this.permissions = permissions;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
