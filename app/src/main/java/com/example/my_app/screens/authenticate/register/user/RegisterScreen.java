package com.example.my_app.screens.authenticate.register.user;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.models.Cart;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.authenticate.LoginScreen;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class RegisterScreen extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvNavToSI;
    Button btnSwitchSU, btnSignUp;
    EditText edtUsername, edtPassword, edtDisplayName;

    boolean isPhone = true;
    UserInfo userInfo = new UserInfo();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        tvNavToSI = (TextView) findViewById(R.id.tvNavToSI);
        btnSwitchSU = (Button)findViewById(R.id.btnSwitchSU) ;
        btnSignUp = (Button)findViewById(R.id.btnSignUp) ;
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtDisplayName = (EditText) findViewById(R.id.edtDisplayName);
        tvNavToSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("nav to login");
                Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
        btnSwitchSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPhone){
                    edtUsername.setHint("Email");
                    edtUsername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_alternate_email_24, 0, 0, 0);
                    btnSwitchSU.setText("Đăng ký với SDT");
                    edtUsername.setInputType(InputType.TYPE_CLASS_TEXT);
                }else {
                    edtUsername.setHint("Phone number");
                    edtUsername.setCompoundDrawablesWithIntrinsicBounds(R.drawable.outline_local_phone_24, 0, 0, 0);
                    btnSwitchSU.setText("Đăng ký với Email");
                    edtUsername.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                isPhone = !isPhone;
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean canNext = true;
                if(edtDisplayName.getText().toString().trim().equals("")){
                    edtDisplayName.setError("Vui lòng nhập tên hiển thị!");
                    edtDisplayName.setFocusable(true);
                    canNext = false;
                }
                if(edtUsername.getText().toString().trim().equals("")){
                    if(isPhone){
                        edtUsername.setError("Vui lòng nhập SDT!");
                    }else {
                        edtUsername.setError("Vui lòng nhập email!");
                    }
                    edtUsername.setFocusable(true);
                    canNext = false;
                }else {
                    if(isPhone){
                        if(!isValidPhoneNumber(edtUsername.getText().toString().trim())){
                            edtUsername.setError("Số điện thoại không hợp lệ!");
                            canNext = false;
                        }
                    }else {
                        if(!isValidEmail(edtUsername.getText().toString().trim())){
                            edtUsername.setError("Email không hợp lệ!");
                            canNext = false;
                        }
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
                if(canNext){
                    userInfo.setDisplayName(edtDisplayName.getText().toString().trim());
                    userInfo.setCreateDate(new Date());
                    userInfo.setRoleId("49dczCwVNYLoChrME3nD");
                    userInfo.setStatus(true);
                    userInfo.setAvatarLink("https://firebasestorage.googleapis.com/v0/b/e-commerce-e6344.appspot.com/o/imageUsers%2Fdefault.png?alt=media&token=c1365a9b-f852-43ca-b9ed-23b8aeccc948");
                    if(isPhone){
                        userInfo.setPhone(edtUsername.getText().toString().trim());
                        Intent intent = new Intent(RegisterScreen.this, VerifyScreen.class);
                        intent.putExtra("user_info", userInfo);
                        intent.putExtra("password", edtPassword.getText().toString().trim());
                        startActivity(intent);
                    }else{
                        userInfo.setEmail(edtUsername.getText().toString().trim());
                        createUserWithEmailAndPassword(userInfo, edtPassword.getText().toString().trim());
                    }
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

    private void verifyEmail(UserInfo user, FirebaseAuth auth){
        auth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"Thư xác nhận đã được gửi đến email của bạn, vui lòng xác nhận để hoàn tất quá trình đăng ký!", Toast.LENGTH_LONG).show();
                                finish();
                        } else {
                            // Handle the error
                            Log.e(TAG, "Error sending email verification", task.getException());
                        }
                    }
                });
    }


    private void createUserWithEmailAndPassword(UserInfo user, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully
                            FirebaseUser user = auth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            userInfo.setUid(user.getUid());
                            db.collection("users").document(user.getUid())
                                    .set(userInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            DocumentReference ref = db.collection("carts").document();
                                            ref.set(new Cart(user.getUid(), ref.getId()));
                                            verifyEmail(userInfo, auth);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Tạo tài khoản không thành công!",
                                                    Toast.LENGTH_LONG).show();
                                            user.delete();
                                        }
                                    });
                        } else {
                            // Handle errors during user creation
                            Toast.makeText(getApplicationContext(),
                                    "Xảy ra lỗi trong quá trình tào tài khoản",
                                    Toast.LENGTH_LONG);
                            Log.e(TAG, "Error creating user", task.getException());
                            finish();
                        }
                    }
                });
    }

}