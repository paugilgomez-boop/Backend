package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "User", description = "Modelo de Usuario del Sistema")
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

    @ApiModelProperty(value = "Identificador único del usuario")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Nombre de usuario")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ApiModelProperty(value = "Contraseña del usuario")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ApiModelProperty(value = "Correo electrónico")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ApiModelProperty(value = "Saldo del usuario")
    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    @ApiModelProperty(value = "Permisos del usuario (PLAYER o ADMIN)")
    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @ApiModelProperty(value = "Nivel del usuario")
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
