package com.example.my_app.screens.authenticate.register.merchandiser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.my_app.R;
import com.example.my_app.models.ShopPending;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.authenticate.LoginScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class RegisterShop extends AppCompatActivity {

    TextView tvNavToSI;
    Button btnNext;
    EditText edtPhone, edtEmail, edtPassword, edtShopName, edtCCCD;
    ShopPending shop= new ShopPending();
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
                }else {
                    if(!isValidPhoneNumber(edtPhone.getText().toString().trim())){
                        edtPhone.setError("Số điện thoại không hợp lệ!");
                        edtPhone.setFocusable(true);
                        canNext = false;
                    }
                }
                if(edtEmail.getText().toString().trim().equals("")){
                    edtEmail.setError("Vui lòng nhập email!");
                    edtEmail.setFocusable(true);
                    canNext = false;
                }else {
                    if(!isValidEmail(edtEmail.getText().toString().trim())){
                        edtEmail.setError("Email không hợp lệ!");
                        edtEmail.setFocusable(true);
                        canNext = false;
                    }
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
                }else {
                    if(edtCCCD.getText().toString().trim().length() != 12){
                        edtCCCD.setError("Độ dài CCCD không hợp lệ!");
                        edtCCCD.setFocusable(true);
                        canNext = false;
                    }
                }

                if(canNext){
                    shop.setDisplayName(edtShopName.getText().toString().trim());
                    shop.setPassword(edtPassword.getText().toString().trim());
                    shop.setCccd(edtCCCD.getText().toString().trim());
                    shop.setPhone(edtPhone.getText().toString().trim());
                    shop.setEmail(edtEmail.getText().toString().trim());
                    shop.setRoleId("s63GofApLdH8B7MZazgQ");
                    shop.setCreateDate(new Date());
                    Intent intent = new Intent(RegisterShop.this, GetCCCDImg.class);
                    intent.putExtra("shop", shop);
                    startActivity(intent);
                }
            }
        });
    }
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Match a number with optional '-' and decimal
        String regex = "^\\d{10}$"; // For ten-digit numbers
        return phoneNumber.matches(regex);
    }
    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(regex);
    }
}