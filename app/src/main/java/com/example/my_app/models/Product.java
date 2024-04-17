package com.example.my_app.models;
import java.util.ArrayList;

public class Product {
    private String productName;
    ArrayList<String> listImageUrl;
    private String desc;
    private String price;
    private int quantity;
    private String categoryName;
    private String productId;
    private String uid;

    public Product(){
    }

    public Product(String productName, ArrayList<String> listImageUrl, String desc, String price, int quantity, String categoryName, String productId, String uid) {
        this.productName = productName;
        this.listImageUrl = listImageUrl;
        this.desc = desc;
        this.price = price;
        this.quantity = quantity;
        this.categoryName = categoryName;
        this.productId = productId;
        this.uid = uid;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ArrayList<String> getListImageUrl() {
        return listImageUrl;
    }

    public void setListImageUrl(ArrayList<String> listImageUrl) {
        this.listImageUrl = listImageUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
