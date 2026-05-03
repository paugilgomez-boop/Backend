package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "Purchase", description = "Registro de Compra")
public class Purchase {
    private String id;
    private String userId;
    private String itemId;
    private int quantity;
    private double totalPrice;
    private String date;

    public Purchase() {
    }

    public Purchase(String id, String userId, String itemId, int quantity, double totalPrice, String date) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.date = date;
    }

    @ApiModelProperty(value = "ID único de la compra")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "ID del usuario que realizó la compra")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @ApiModelProperty(value = "ID del item comprado")
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @ApiModelProperty(value = "Cantidad de items comprados")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @ApiModelProperty(value = "Precio total de la compra")
    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @ApiModelProperty(value = "Fecha de la compra")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
