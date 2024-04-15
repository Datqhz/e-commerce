package com.example.my_app.screens.authenticate.register.merchandiser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.my_app.R;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.authenticate.LoginScreen;

import java.util.Date;

public class RegisterShop extends AppCompatActivity {

    TextView tvNavToSI;
    Button btnNext;
    EditText edtPhone, edtEmail, edtPassword, edtShopName, edtCCCD;
    UserInfo userInfo = new UserInfo();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_shop);
        setControl();
        setEvent();
    }
    private void setControl(){
        tvNavToSI = (TextView) findViewById(R.id.tvNavToSI);
        btnNext = (Button)findViewById(R.id.btnNext) ;
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtCCCD = (EditText) findViewById(R.id.edtCCCD);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtShopName = (EditText) findViewById(R.id.edtShopName);
    }
    private void setEvent(){
        tvNavToSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("nav to login");
                Intent intent = new Intent(RegisterShop.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean canNext = true;
                if(edtShopName.getText().toString().trim().equals("")){
                    edtShopName.setError("Vui lòng nhập tên shop!");
                    edtShopName.setFocusable(true);
                    canNext = false;
                }
                if(edtPhone.getText().toString().trim().equals("")){
                    edtPhone.setError("Vui lòng nhập SDT!");
                    edtPhone.setFocusable(true);
                    canNext = false;
                }
                if(edtEmail.getText().toString().trim().equals("")){
                    edtEmail.setError("Vui lòng nhập email!");
                    edtEmail.setFocusable(true);
                    canNext = false;
                }
                if(edtPassword.getText().toString().trim().equals("")){
                    edtPassword.setError("Vui lòng nhập mật khẩu!");
                    edtPassword.setFocusable(true);
                    canNext = false;
                }else {
                    if(edtPassword.getText().toString().trim().length() <6){
                        edtPassword.setError("Vui lòng nhập mật khẩu có độ dài từ 6 chữ số!");
                        edtPassword.setFocusable(true);
                        canNext = false;
                    }
                }
                if(edtCCCD.getText().toString().trim().equals("")){
                    edtCCCD.setError("Vui lòng nhập số CCCD!");
                    edtCCCD.setFocusable(true);
                    canNext = false;
                }

                if(canNext){
                    userInfo.setDisplayName(edtShopName.getText().toString().trim());
                    userInfo.setPassword(edtPassword.getText().toString().trim());
                    userInfo.setCccd(edtCCCD.getText().toString().trim());
                    userInfo.setPhone(edtPhone.getText().toString().trim());
                    userInfo.setEmail(edtEmail.getText().toString().trim());
                    userInfo.setRoleId("s63GofApLdH8B7MZazgQ");
                    userInfo.setCreateDate(new Date());
                    Intent intent = new Intent(RegisterShop.this, GetCCCDImg.class);
                    intent.putExtra("user_info", userInfo);
                    startActivity(intent);
                }
            }
        });
    }
}