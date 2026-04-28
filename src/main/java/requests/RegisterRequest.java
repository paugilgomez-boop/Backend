package requests;

public class RegisterRequest {
    private String id;
    private String username;
    private String password;
    private String email;
    private double saldo;
    private String role;
    private int level;
    private String permissions;

    public RegisterRequest() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
