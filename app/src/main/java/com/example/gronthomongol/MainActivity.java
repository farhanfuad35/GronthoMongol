package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.backendless.Backendless;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Backendless.setUrl(CREDENTIALS.getServerUrl());
        Backendless.initApp(getApplicationContext(), CREDENTIALS.getApplicationId(), CREDENTIALS.getApiKey());

        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
        finish();
    }
}