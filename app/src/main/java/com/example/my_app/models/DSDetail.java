package com.example.my_app.models;

import java.util.Date;

public class DSDetail {
    private String statusId;
    private String orderId;
    private Date dateOfStatus;

    public DSDetail() {
    }

    public DSDetail(String statusId, String orderId, Date dateOfStatus) {
        this.statusId = statusId;
        this.orderId = orderId;
        this.dateOfStatus = dateOfStatus;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getDateOfStatus() {
        return dateOfStatus;
    }

    public void setDateOfStatus(Date dateOfStatus) {
        this.dateOfStatus = dateOfStatus;
    }
}
