package models;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
