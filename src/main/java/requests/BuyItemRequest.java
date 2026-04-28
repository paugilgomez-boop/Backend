package requests;

public class BuyItemRequest {
    private String itemId;
    private int quantity;

    public BuyItemRequest() {
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
