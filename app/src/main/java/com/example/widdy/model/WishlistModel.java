package com.example.widdy.model;

public class WishlistModel {

    private String name;
    private String date;
    private String notes;
    private String imageUrl;
    private String userId;
    private int items;
    private int accessCode;

    public WishlistModel() {}

    public WishlistModel(String name, int items) {
        this.name = name;
        this.items = items;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
    public String getNotes() { return notes; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }
    public int getItems() { return items; }
    public int getAccessCode() { return accessCode; }
}
