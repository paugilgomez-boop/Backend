package requests;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "ItemRequest", description = "Solicitud de Creación/Modificación de Item")
public class ItemRequest {
    private int id;
    private String name;
    private String description;
    private String type;
    private double price;
    private boolean available = true;
    private String assetName;

    public ItemRequest() {
    }

    @ApiModelProperty(value = "ID del item")
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @ApiModelProperty(value = "Nombre del item")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @ApiModelProperty(value = "Descripción del item")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @ApiModelProperty(value = "Tipo de item")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @ApiModelProperty(value = "Precio del item")
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @ApiModelProperty(value = "Disponibilidad")
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @ApiModelProperty(value = "Nombre del asset")
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
}
