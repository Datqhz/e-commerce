package com.example.my_app.screens.authenticate.register.user;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import com.example.my_app.R;
import com.example.my_app.models.UserInfo;
import com.example.my_app.screens.authenticate.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VerifyScreen extends AppCompatActivity {
    UserInfo userInfo;
    Button btnVerify, btnResend;
    EditText edtOTP;
    TextView tvVerifyContent;
    String password;

    String otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_screen);
        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getSerializableExtra("user_info");
        password = (String) intent.getSerializableExtra("password");
        setControl();
        setEvent();
        verifyPhoneNumber();

    }

    private void setControl(){
        tvVerifyContent = findViewById(R.id.tvVerifyContent);
        btnVerify = findViewById(R.id.btnVerify);
        edtOTP = findViewById(R.id.edtOTP);
        btnResend = findViewById(R.id.btnResend);

    }
    private void setEvent(){
        tvVerifyContent.setText("OTP đã được gửi đến số điện thoại "+userInfo.getPhone());
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyPhoneNumber();
                Toast.makeText(getApplicationContext(), "Mã OTP đang được gửi đến bạn", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void verifyPhoneNumber() {
        String phone = "+84"+userInfo.getPhone().substring(1);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        otp = phoneAuthCredential.getSmsCode();
                        System.out.println("otp: "  + otp);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(), "Xảy ra lỗi trong quá trình gửi SMS", Toast.LENGTH_LONG).show();
                        System.out.println("send message fail: " + e.getMessage());
                    }
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void verifyCode() {
        if(edtOTP.getText().toString().trim().equals("")){
            Toast.makeText(VerifyScreen.this, "Vui lòng nhập mã OTP.", Toast.LENGTH_LONG).show();
        }else {
            if(edtOTP.getText().toString().trim().length()!=6){
                Toast.makeText(VerifyScreen.this, "Độ dài OTP không hợp lệ.", Toast.LENGTH_LONG).show();
            }else {
                if(otp.equals(edtOTP.getText().toString().trim())){
                    System.out.println("start create");
                    Executors.newSingleThreadExecutor().submit(this::createUserWithEmailAndPassword);
//            CompletableFuture.runAsync(this::createUserWithEmailAndPassword);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }else {
                    Toast.makeText(VerifyScreen.this, "Mã xác thực sai hoặc đã quá hạn.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void createUserWithEmailAndPassword() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(userInfo.getPhone()+"@gmail.com",password)
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
                                            System.out.println("create success");
                                            Toast.makeText(getApplicationContext(), "Create account successful!", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Create account failure!", Toast.LENGTH_LONG).show();
                                            user.delete();
                                        }
                                    });

                        } else {
                            // Handle errors during user creation
                            Toast.makeText(getApplicationContext(),"Error creating user", Toast.LENGTH_LONG);
                            System.out.println("create fail");
                            Log.e(TAG, "Error creating user", task.getException());
                            finish();
                        }
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}