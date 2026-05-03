package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "Item", description = "Modelo de Item en el Catálogo")
public class Item {
    private String id;
    private String name;
    private String description;
    private String type;
    private double price;
    private boolean available;
    private String assetName;

    public Item() {
    }

    public Item(String id, String name, String description, String type, double price) {
        this(id, name, description, type, price, true, null);
    }

    public Item(String id, String name, String description, String type, double price, boolean available, String assetName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.available = available;
        this.assetName = assetName;
    }

    @ApiModelProperty(value = "Identificador único del item")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Nombre del item")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "Descripción del item")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "Tipo de item")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ApiModelProperty(value = "Precio del item")
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @ApiModelProperty(value = "Disponibilidad del item")
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @ApiModelProperty(value = "Nombre del recurso/asset del item")
    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
