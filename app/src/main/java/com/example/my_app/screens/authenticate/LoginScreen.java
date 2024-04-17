package com.example.my_app.screens.authenticate;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_app.R;
import com.example.my_app.screens.authenticate.register.user.RegisterScreen;
import com.example.my_app.screens.admin.ShopPendingListScreen;
import com.example.my_app.SplashScreen;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.merchandiser.ProductManagement;
import com.example.my_app.shared.GlobalVariable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity {


    TextView tvNavToSU, tvForgotPassword;
    EditText edtLoginUsername, edtLoginPassword;
    Button btnLogin;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        setControl();
        setEvent();
    }
    private void setControl(){
        tvNavToSU = (TextView) findViewById(R.id.tvNavToSU);
        tvForgotPassword = (TextView) findViewById(R.id.LoginScreen_tvForgotPassword);
        edtLoginUsername = (EditText) findViewById(R.id.edtLoginUsername);
        edtLoginPassword = (EditText) findViewById(R.id.edtLoginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
    }
    private void setEvent(){
        tvNavToSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginScreen.this, RegisterScreen.class);
                startActivity(intent);
                finish();
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginScreen.this, ForgotPasswordScreen.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean canNext = true;
                if(edtLoginUsername.getText().toString().trim().equals("")){
                    edtLoginUsername.setError("Vui lòng nhập email hoặc sdt!");
                    edtLoginUsername.setFocusable(true);
                    canNext = false;
                }
                if(edtLoginPassword.getText().toString().trim().equals("")){
                    edtLoginPassword.setError("Vui lòng nhập mật khẩu!");
                    edtLoginPassword.setFocusable(true);
                    canNext = false;
                }else {
                    if(edtLoginPassword.getText().toString().trim().length() <6){
                        edtLoginPassword.setError("Vui lòng nhập mật khẩu có độ dài từ 6 chữ số!");
                        edtLoginPassword.setFocusable(true);
                        canNext = false;
                    }
                }
                if(canNext){
                    FirebaseAuth auth = FirebaseAuth.getInstance();
//                    auth.signOut();
                    if(isValidPhoneNumber(edtLoginUsername.getText().toString().trim())){
                        auth.signInWithEmailAndPassword(edtLoginUsername.getText()
                                .toString().trim()+"@gmail.com",edtLoginPassword.getText().toString()
                                .trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                db.collection("users").document(authResult.getUser()
                                                .getUid()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        UserInfo userInfo = document.toObject(UserInfo.class);
                                                        UserInfo user = new UserInfo();
                                                        GlobalVariable globalVariable = new GlobalVariable(user);
                                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                        //////Nav to suitable screen with roleId
                                                        if(userInfo.getRoleId().equals("SJBifnfNKREVcjRmZw9X")){ //admin
                                                            System.out.println("admin");
                                                            Intent intent = new Intent(LoginScreen.this, ShopPendingListScreen.class);
                                                            startActivity(intent);
                                                        }else if(userInfo.getRoleId().equals("49dczCwVNYLoChrME3nD")){ // shopper
                                                            System.out.println("buyer");
                                                            Intent intent = new Intent(LoginScreen.this, SplashScreen.class);
                                                            startActivity(intent);
                                                        }else {// Merchandiser
                                                            System.out.println("mer");
                                                            Intent intent = new Intent(LoginScreen.this, ProductManagement.class);
                                                            startActivity(intent);
                                                        }
                                                        finish();
                                                    } else {
                                                        Log.d(TAG, "No such document");
                                                    }

                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginScreen.this,"Số điện thoại hoặc mật khẩu sai!", Toast.LENGTH_LONG).show();

                                    }
                                });
                    }else if(isValidEmail(edtLoginUsername.getText().toString().trim())){
                        auth.signInWithEmailAndPassword(edtLoginUsername.getText()
                                .toString().trim(),edtLoginPassword.getText().toString()
                                .trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                System.out.println("login success");
                                if(auth.getCurrentUser().isEmailVerified()){
                                    db.collection("users").document(authResult.getUser()
                                                    .getUid()).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            UserInfo userInfo = document.toObject(UserInfo.class);
                                                            GlobalVariable globalVariable = new GlobalVariable(userInfo);
                                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                            //////Nav to suitable screen with roleId
                                                            if(userInfo.getRoleId().equals("SJBifnfNKREVcjRmZw9X")){ //admin
                                                                Intent intent = new Intent(LoginScreen.this, ShopPendingListScreen.class);
                                                                startActivity(intent);
                                                            }else if(userInfo.getRoleId().equals("49dczCwVNYLoChrME3nD")){ // shopper
                                                                Intent intent = new Intent(LoginScreen.this, SplashScreen.class);
                                                                startActivity(intent);
                                                            }else {// Merchandiser
                                                                Intent intent = new Intent(LoginScreen.this, ProductManagement.class);
                                                                startActivity(intent);
                                                            }
                                                            finish();
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }

                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                }else {
                                    auth.signOut();
                                    Toast.makeText(LoginScreen.this,"Vui lòng xác nhận email!", Toast.LENGTH_LONG).show();
                                    System.out.println("not verify");
                                }

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginScreen.this,"Email hoặc mật khẩu sai!", Toast.LENGTH_LONG).show();

                                    }
                                });
                    }else {
                        Toast.makeText(LoginScreen.this, "Username bạn nhập vào không hợp lệ!", Toast.LENGTH_LONG).show();;
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
}