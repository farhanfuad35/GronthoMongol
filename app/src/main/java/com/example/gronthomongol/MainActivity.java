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

        Backendless.setUrl(CREDENTIALS.SERVER_URL);
        Backendless.initApp(getApplicationContext(), CREDENTIALS.APPLICATION_ID, CREDENTIALS.API_KEY);

        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }
}