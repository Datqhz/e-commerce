package com.example.my_app.models;

public class Cart {
    private String cartId;
    private String uid;

    public Cart() {
    }

    public Cart(String uid, String cartId) {
        this.uid = uid;
        this.cartId = cartId;
    }

    public String getUid() {
        return uid;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
}
