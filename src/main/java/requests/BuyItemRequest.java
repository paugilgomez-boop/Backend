package requests;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ApiModel(value = "BuyItemRequest", description = "Solicitud de Compra de Item")
public class BuyItemRequest {
    private String itemId;
    private int quantity;

    public BuyItemRequest() {
    }

    @ApiModelProperty(value = "ID del item a comprar")
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    @ApiModelProperty(value = "Cantidad a comprar")
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
