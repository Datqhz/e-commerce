package com.example.my_app.models;

public class Address {
    private String addressId;
    private String uid;
    private String address;
    private boolean isDefault;

    public Address() {
    }

    public Address(String addressId, String uid, String address, boolean isDefault) {
        this.addressId = addressId;
        this.uid = uid;
        this.address = address;
        this.isDefault = isDefault;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
