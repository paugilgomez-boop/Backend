package requests;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "RegisterRequest", description = "Solicitud de Registro de Usuario")
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

    @ApiModelProperty(value = "ID del usuario")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @ApiModelProperty(value = "Nombre de usuario")
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @ApiModelProperty(value = "Contraseña")
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @ApiModelProperty(value = "Email")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @ApiModelProperty(value = "Saldo inicial")
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    @ApiModelProperty(value = "Rol del usuario")
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @ApiModelProperty(value = "Nivel inicial")
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    @ApiModelProperty(value = "Permisos")
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
