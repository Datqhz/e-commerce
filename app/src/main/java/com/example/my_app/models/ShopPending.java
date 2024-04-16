package com.example.my_app.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ShopPending implements Serializable {
    private String shopId;
    private String displayName;
    private String cccd;
    private String phone;
    private String email;
    private Date createDate;
    private List<String> CCCDImg;
    private String password;
    private String roleId;

    public ShopPending(String shopId, String displayName, String cccd, String phone, String email, Date createDate, List<String> CCCDImg, String password, String roleId) {
        this.shopId = shopId;
        this.displayName = displayName;
        this.cccd = cccd;
        this.phone = phone;
        this.email = email;
        this.createDate = createDate;
        this.CCCDImg = CCCDImg;
        this.password = password;
        this.roleId = roleId;
    }

    public ShopPending() {
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public List<String> getCCCDImg() {
        return CCCDImg;
    }

    public void setCCCDImg(List<String> CCCDImg) {
        this.CCCDImg = CCCDImg;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "ShopPending{" +
                "shopId='" + shopId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", cccd='" + cccd + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", createDate=" + createDate +
                ", CCCDImg=" + CCCDImg +
                ", password='" + password + '\'' +
                ", roleId='" + roleId + '\'' +
                '}';
    }
}
