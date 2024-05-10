package com.example.my_app.screens.authenticate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.my_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordScreen extends AppCompatActivity {

    ImageButton btnPrevious;
    Button btnOk;
    EditText edtUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);
        setControl();
        setEvent();
    }

    private void setControl() {
        btnOk = findViewById(R.id.ForgotPassword_btnOK);
        btnPrevious = findViewById(R.id.ForgotPassword_btnPrevious);
        edtUsername = findViewById(R.id.ForgotPassword_edtUsername);
    }

    private void setEvent() {
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtUsername.getText().toString().trim().equals("")) {
                    edtUsername.setError("Vui lòng nhập tên đăng nhập!");
                } else {
                    if (isValidEmail(edtUsername.getText().toString().trim())) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(edtUsername.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext(),
                                        "Email thay đổi mật khẩu đã được gửi đến email của bạn.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    } else if (isValidPhoneNumber(edtUsername.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(),
                                "Tính năng quên mật khẩu chưa được hỗ trợ trên phương thức đăng nhập bằng số điện thoại.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Tên đăng nhập bạn nhập vào không hợp lệ. Vui lòng nhập lại!",
                                Toast.LENGTH_LONG).show();
                        edtUsername.setFocusable(true);

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