package com.example.my_app.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String category;
    private String imageUrl;
    private String categoryId;

    public Category(){
    }

    public Category(String category, String imageUrl, String categoryId) {
        this.category = category;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
