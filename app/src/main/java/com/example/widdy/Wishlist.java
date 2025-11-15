package com.example.widdy;

public class Wishlist {
    private int id;
    private String name;
    private int itemCount;
    private String color;

    public Wishlist(int id, String name, int itemCount, String color) {
        this.id = id;
        this.name = name;
        this.itemCount = itemCount;
        this.color = color;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getItemCount() { return itemCount; }
    public String getColor() { return color; }
}
