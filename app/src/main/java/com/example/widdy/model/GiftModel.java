package com.example.widdy.model;

public class GiftModel {

    private String ID;
    private String name;
    private String imageUrl;
    private String expectedPrice;
    private String description;
    private String storeLocation;
    private String productLink;
    private String priority;
    private long createdAt;
    private boolean isReserved;
    private String reservedBy;

    public GiftModel() {
    }

    public GiftModel(String ID, String name, String imageUrl, String expectedPrice,
                     String description, String storeLocation, String productLink,
                     String priority, long createdAt) {
        this.ID = ID;
        this.name = name;
        this.imageUrl = imageUrl;
        this.expectedPrice = expectedPrice;
        this.description = description;
        this.storeLocation = storeLocation;
        this.productLink = productLink;
        this.priority = priority;
        this.createdAt = createdAt;
        this.isReserved = false;
        this.reservedBy = "";
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getExpectedPrice() {
        return expectedPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public String getProductLink() {
        return productLink;
    }

    public String getPriority() {
        return priority;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public String getReservedBy() {
        return reservedBy;
    }
}
