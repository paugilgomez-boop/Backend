package responses;

public class EventUserResponse {
    private String nombre;
    private String apellidos;
    private String imagen;

    public EventUserResponse() {
    }

    public EventUserResponse(String nombre, String apellidos, String imagen) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
