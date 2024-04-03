package com.example.my_app;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.my_app.models.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.my_app.databinding.ActivityVerifyScreenBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class VerifyScreen extends AppCompatActivity {
    UserInfo userInfo;
    Button btnVerify, btnResendOTP;
    EditText edtOTP;
    TextView tvVerifyContent;

    String otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_screen);
        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getSerializableExtra("user_info");
        setControl();
        setEvent();
        verifyPhoneNumber();

    }

    private void setControl(){
        tvVerifyContent = findViewById(R.id.tvVerifyContent);
        btnVerify = findViewById(R.id.btnVerify);
        btnResendOTP = findViewById(R.id.btnResendOTP);
        edtOTP = findViewById(R.id.edtOTP);

    }
    private void setEvent(){
        tvVerifyContent.setText("OTP đã được gửi đến số điện thoại "+userInfo.getPhone());
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
        btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        if(otp.equals(edtOTP.getText().toString().trim())){
            System.out.println("start create");
            Executors.newSingleThreadExecutor().submit(this::createUserWithEmailAndPassword);
//            CompletableFuture.runAsync(this::createUserWithEmailAndPassword);
            System.out.println("created");
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }else {
            Toast.makeText(VerifyScreen.this, "Mã xác thực sai hoặc đã quá hạn.", Toast.LENGTH_LONG).show();
        }
    }
    private void createUserWithEmailAndPassword() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(userInfo.getPhone()+"@gmail.com", userInfo.getPassword())
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