package com.example.my_app.models;

public class Cart {
    private String uid;

    public Cart(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
