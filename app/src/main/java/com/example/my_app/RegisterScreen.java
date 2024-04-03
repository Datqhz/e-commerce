package com.example.my_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.models.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
                }

                if(edtPassword.getText().toString().trim().equals("")){
                    edtPassword.setError("Vui lòng nhập mật khẩu!");
                    edtPassword.setFocusable(true);
                    canNext = false;
                }
                if(canNext){
                    userInfo.setDisplayName(edtDisplayName.getText().toString().trim());
                    userInfo.setPassword(edtPassword.getText().toString().trim());
                    userInfo.setCreateDate(new Date());
                    userInfo.setRoleId("49dczCwVNYLoChrME3nD");
                    if(isPhone){
                        userInfo.setPhone(edtUsername.getText().toString().trim());
                        Intent intent = new Intent(RegisterScreen.this, VerifyScreen.class);
                        intent.putExtra("user_info", userInfo);
                        startActivity(intent);
                    }else{
                        userInfo.setEmail(edtUsername.getText().toString().trim());
                        createUserWithEmailAndPassword(userInfo);
                    }
                }
            }
        });
    }

    private void verifyEmail(UserInfo user, FirebaseAuth auth){
        auth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                                Toast.makeText(RegisterScreen.this,"Thư xác nhận đã được gửi đến email của bạn, vui lòng xác nhận để hoàn tất quá trình đăng ký!", Toast.LENGTH_LONG).show();
                        } else {
                            // Handle the error
                            Log.e(TAG, "Error sending email verification", task.getException());
                        }
                    }
                });
    }


    private void createUserWithEmailAndPassword(UserInfo user) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully
                            FirebaseUser user = auth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user.getUid())
                                    .set(userInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
//                                            finish();
                                            Toast.makeText(getApplicationContext(), "Tạo tài khoản thành công!", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Tạo tài khoản không thành công!", Toast.LENGTH_LONG).show();
                                            user.delete();
                                        }
                                    });
                            verifyEmail(userInfo, auth);
                        } else {
                            // Handle errors during user creation
                            Toast.makeText(getApplicationContext(),"Xảy ra lỗi trong quá trình tào tài khoản", Toast.LENGTH_LONG);
                            Log.e(TAG, "Error creating user", task.getException());
                            finish();
                        }
                    }
                });
    }

}