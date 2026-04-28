package models;

public class Inventory {
    private String playerId;
    private String itemId;
    private String type;
    private int quantity;

    public Inventory() {
    }

    public Inventory(String playerId, String itemId, String type, int quantity) {
        this.playerId = playerId;
        this.itemId = itemId;
        this.type = type;
        this.quantity = quantity;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
