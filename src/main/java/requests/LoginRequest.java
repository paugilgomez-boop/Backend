package requests;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "LoginRequest", description = "Solicitud de Login")
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {
    }

    @ApiModelProperty(value = "Nombre de usuario")
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @ApiModelProperty(value = "Contraseña")
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
