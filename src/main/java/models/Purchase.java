package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "Purchase", description = "Registro de Compra")
public class Purchase {
    private int id;
    private int userId;
    private int itemId;
    private int quantity;
    private double totalPrice;
    private double userSaldo;
    private String date;

    public Purchase() {
    }

    public Purchase(int id, int userId, int itemId, int quantity, double totalPrice, double userSaldo, String date) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.userSaldo = userSaldo;
        this.date = date;
    }

    @ApiModelProperty(value = "ID único de la compra")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ApiModelProperty(value = "ID del usuario que realizó la compra")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @ApiModelProperty(value = "ID del item comprado")
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
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

    @ApiModelProperty(value = "Saldo restante del usuario tras la compra")
    public double getUserSaldo() {
        return userSaldo;
    }

    public void setUserSaldo(double userSaldo) {
        this.userSaldo = userSaldo;
    }

    @ApiModelProperty(value = "Fecha de la compra")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
