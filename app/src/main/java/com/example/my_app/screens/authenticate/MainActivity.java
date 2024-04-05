package com.example.my_app.screens.authenticate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.my_app.R;
import com.example.my_app.screens.authenticate.register.user.RegisterScreen;
import com.example.my_app.screens.authenticate.register.merchandiser.RegisterShop;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp, btnSignUpShop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setControl();
        setEvent();
    }

    private void setControl(){
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignUp = (Button)findViewById(R.id.btnSignUp);
        btnSignUpShop = (Button)findViewById(R.id.btnSignUpShop);
    }
    private void setEvent(){
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginScreen.class);
                startActivity(intent);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterScreen.class);
                startActivity(intent);
            }
        });
        btnSignUpShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterShop.class);
                startActivity(intent);
            }
        });
    }
}