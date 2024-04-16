package com.example.my_app.shared;

import com.example.my_app.models.UserInfo;
import com.google.firebase.firestore.auth.User;

public class GlobalVariable {
    public static UserInfo userInfo;

    public GlobalVariable(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public GlobalVariable() {
    }

    public static UserInfo getUserInfo() {
        return userInfo;
    }

    public static void setUserInfo(UserInfo userInfo) {
        GlobalVariable.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return userInfo.toString();
    }
}
