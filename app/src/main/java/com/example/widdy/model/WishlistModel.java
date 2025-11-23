package com.example.widdy.model;

public class WishlistModel {

    private String documentId;
    private String name;
    private String date;
    private String notes;
    private String imageUrl;
    private String userId;
    private long accessCode;
    private long createdAt;
    private int itemCount;

    public WishlistModel() {
    }

    public WishlistModel(String name, String date, String notes, String imageUrl, String userId, long accessCode, long createdAt) {
        this.name = name;
        this.date = date;
        this.notes = notes;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.accessCode = accessCode;
        this.createdAt = createdAt;
        this.itemCount = 0;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(long accessCode) {
        this.accessCode = accessCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}