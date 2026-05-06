package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "Inventory", description = "Inventario de un Usuario")
public class Inventory {
    private int userId;
    private int itemId;
    private int quantity;

    public Inventory() {
    }

    public Inventory(int userId, int itemId, int quantity) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    @ApiModelProperty(value = "ID del usuario propietario del inventario")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @ApiModelProperty(value = "ID del item en el inventario")
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @ApiModelProperty(value = "Cantidad del item en el inventario")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
