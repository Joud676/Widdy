package com.example.widdy.model;

public class WishlistModel {

    String title;
    int items;

    public WishlistModel() { }

    public WishlistModel(String title, int items) {
        this.title = title;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public int getItems() {
        return items;
    }
}
