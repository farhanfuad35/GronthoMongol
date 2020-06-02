package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.push.DeviceRegistrationResult;

import java.util.ArrayList;
import java.util.List;

public class login extends AppCompatActivity {

    private EditText etPassword;
    private EditText etEmail;
    private Button btnLogin;
    private TextView tvSignup;
    private TextView tvForgetpassword;
    private CheckBox cbStayLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeGUIElements();    // Get Element IDs

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSignup.setTextColor(Color.BLUE);
                Intent intent = new Intent(view.getContext(),com.example.gronthomongol.register.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                btnLogin.setText("Logging in...");
                final String email = etEmail.getText().toString().toLowerCase();
                final String password = etPassword.getText().toString();
                //final Boolean stayLoggedIn = cbStayLoggedIn.isChecked();
                final Boolean stayLoggedIn = true;

                Login(email, password, stayLoggedIn);



                Log.i("deviceid","hewweoewhi");
//                BackendlessAPIMethods.Login(getApplicationContext(), email, password, stayLoggedIn, getApplicationContext().getAct);

            }
        });
    }



    private void Login(final String email, final String password, Boolean stayLoggedIn)
    {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
//                CONSTANTS.setCurrentUserEmail(email);
//                CONSTANTS.setCurrentSavedUser(response);
//
//                FileMethods.writes(getApplicationContext(), email);
//                System.out.println("logged in "+email);
//
//                Log.i("deviceid", "Login Successful");

                // If login successful, register user to the database with deviceID

                // registerDeviceForNotification();         //TODO

                Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
                btnLogin.setText("Login");

                Log.i("deviceid", fault.getMessage());
            }
        }, stayLoggedIn);
    }


    private void registerDeviceForNotification() {

        // Testing Backendless and FCM Service

        List<String> channels = new ArrayList<String>();
        channels.add( "default" );
        Backendless.Messaging.registerDevice(channels, new AsyncCallback<DeviceRegistrationResult>() {
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                // Toast.makeText( Login.this, "Device registered!", Toast.LENGTH_LONG).show();
                Log.i("deviceid", "Device registered successfully on backendless");

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText( login.this, "Error registering " + fault.getMessage(),
                        Toast.LENGTH_LONG).show();

                Log.i("deviceid", fault.getMessage());
            }
        });

        Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
            @Override
            public void handleResponse(DeviceRegistration response) {

                BackendlessUser user = Backendless.UserService.CurrentUser();


                //BackendlessAPIMethods.updateDeviceId(login.this, user, response.getDeviceId());

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });

    }


    private void initializeGUIElements()
    {
        tvSignup = findViewById(R.id.tvLogin_createaccount);
        etEmail = findViewById(R.id.etLogin_email);
        etPassword = findViewById(R.id.etLogin_password);
        btnLogin = findViewById(R.id.btnLogin_Login);
        tvForgetpassword = findViewById(R.id.tvLogin_ForgetPassword);
        cbStayLoggedIn = findViewById(R.id.cbLogin_StayLoggedIn);
    }
}