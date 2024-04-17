package com.example.my_app.models;

public class Category {
    private String categoryName;
    private String imageUrl;
    private String categoryId;

    public Category(){
    }

    public Category(String categoryName, String imageUrl, String categoryId) {
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
