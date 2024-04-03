package com.example.my_app.dto;

import com.example.my_app.models.UserInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserDTO implements Serializable {
    private String id;
    private String displayName;
    private String cccd;
    private String phone;
    private String email;
    private Date createDate;
    private String password;
    private List<String> CCCDImg;
    private String roleId;


    public UserDTO(String id, String displayName, String cccd, String phone, String email, Date createDate, String password, List<String> CCCDImg, String roleId) {
        this.id = id;
        this.displayName = displayName;
        this.cccd = cccd;
        this.phone = phone;
        this.email = email;
        this.createDate = createDate;
        this.password = password;
        this.CCCDImg = CCCDImg;
        this.roleId = roleId;
    }
    public UserDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getCCCDImg() {
        return CCCDImg;
    }

    public void setCCCDImg(List<String> CCCDImg) {
        this.CCCDImg = CCCDImg;
    }
    public void mapToDTO(UserInfo user){
        this.displayName = user.getDisplayName();
        this.cccd = user.getCccd();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.createDate = user.getCreateDate();
        this.password = user.getPassword();
        this.CCCDImg = user.getCCCDImg();
        this.roleId = user.getRoleId();
    }

}
