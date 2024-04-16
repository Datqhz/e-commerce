package com.example.my_app.models;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserInfo implements Serializable {

    private String uid;
    private String displayName;
    private String cccd;
    private String phone;
    private String email;
    private Date createDate;
    private String avatarLink;
    private List<String> CCCDImg;
    private String roleId;
    private boolean status;

    public UserInfo(String uid, String displayName, String cccd, String phone, String email, Date createDate, String avatarLink, List<String> CCCDImg, String roleId, boolean status) {
        this.uid = uid;
        this.displayName = displayName;
        this.cccd = cccd;
        this.phone = phone;
        this.email = email;
        this.createDate = createDate;
        this.avatarLink = avatarLink;
        this.CCCDImg = CCCDImg;
        this.roleId = roleId;
        this.status = status;
    }

    public UserInfo() {
    }


    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatarLink() {
        return avatarLink;
    }

    public void setAvatarLink(String avatarLink) {
        this.avatarLink = avatarLink;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setCCCDImg(List<String> CCCDImg) {
        this.CCCDImg = CCCDImg;
    }
    public void mapFromShopPendingToUser(ShopPending shop){
        this.displayName = shop.getDisplayName();
        this.cccd = shop.getCccd();
        this.phone = shop.getPhone();
        this.email = shop.getEmail();
        this.createDate = shop.getCreateDate();
        this.CCCDImg = shop.getCCCDImg();
        this.roleId = shop.getRoleId();
        this.status = true;
    }
}
