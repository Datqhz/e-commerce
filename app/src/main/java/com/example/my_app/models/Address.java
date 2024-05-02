package com.example.my_app.models;

public class Address {
    private String addressId;
    private String uid;
    private String address;

    public Address() {
    }

    public Address(String addressId, String uid, String address) {
        this.addressId = addressId;
        this.uid = uid;
        this.address = address;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
