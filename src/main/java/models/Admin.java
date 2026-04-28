package models;

public class Admin extends User {
    private String permissions;

    public Admin() {
    }

    public Admin(String id, String username, String password, String email, double saldo, String permissions) {
        super(id, username, password, email, saldo);
        this.permissions = permissions;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}
