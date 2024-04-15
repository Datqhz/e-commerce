package com.example.my_app.shared;

import com.example.my_app.dto.UserDTO;
import com.google.firebase.firestore.auth.User;

public class GlobalVariable {
    public static UserDTO userInfo;

    public GlobalVariable(UserDTO userInfo) {
        this.userInfo = userInfo;
    }

    public GlobalVariable() {
    }

    @Override
    public String toString() {
        return userInfo.toString();
    }
}
