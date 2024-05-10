package com.example.my_app.models;

import java.io.Serializable;
import java.util.Date;

public class Orders implements Serializable {
    private String orderId;
    private Date createDate;
    private String address;
    private String uid;
    private String paymentMethod;

    public Orders() {
    }

    public Orders(String orderId, Date createDate, String address, String uid) {
        this.orderId = orderId;
        this.createDate = createDate;
        this.address = address;
        this.uid = uid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
