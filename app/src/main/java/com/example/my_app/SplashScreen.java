package com.example.my_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.my_app.shared.GlobalVariable;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        GlobalVariable test = new GlobalVariable();
        System.out.println(test);
    }
}