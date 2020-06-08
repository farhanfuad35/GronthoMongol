package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.backendless.persistence.DataQueryBuilder;
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
    private TextView tvInvalidLogin;

    private String email;
    private String password;

    private Dialog waitDialog;


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

                tvInvalidLogin.setVisibility(View.INVISIBLE);

                if(getValues()) {
                    if (!isEmailValid(email)) {
                        // can't proceed for loggin in
                        //etEmail.setError("Please enter a valid email address");
                        tvInvalidLogin.setText("Please enter a valid email address");
                        tvInvalidLogin.setVisibility(View.VISIBLE);
                    }

                    else {
                        final String email = etEmail.getText().toString().toLowerCase();
                        final String password = etPassword.getText().toString();
                        //final Boolean stayLoggedIn = cbStayLoggedIn.isChecked();
                        final Boolean stayLoggedIn = true;

                        waitDialog = new Dialog(login.this);
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_logging_in);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Login(email, password, stayLoggedIn, waitDialog);
                            }
                        });

                        thread.start();

                        Log.i("deviceid", "hewweoewhi");
//                BackendlessAPIMethods.Login(getApplicationContext(), email, password, stayLoggedIn, getApplicationContext().getAct);
                    }
                }
            }
        });

        tvForgetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, forgotPassword.class);
                startActivity(intent);
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Return true if gets values successfully
    private boolean getValues(){
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString();

        if(email.isEmpty()){
            //etEmail.setError("Please enter your email address");
            tvInvalidLogin.setText("Please enter your email address");
            tvInvalidLogin.setVisibility(View.VISIBLE);
            return false;
        }
        else if(password.isEmpty()){
            //etPassword.setError("Please enter password");
            tvInvalidLogin.setText("Please enter password");
            tvInvalidLogin.setVisibility(View.VISIBLE);
            return false;
        }
        else
            return true;
    }


    private void Login(final String email, final String password, Boolean stayLoggedIn, final Dialog dialog)
    {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {

                boolean isAdmin = (boolean) response.getProperty("admin");
                String channel;         // Backendless Notification Channel
                if(isAdmin)
                    channel = "admin";
                else
                    channel = "client";

                registerDeviceForNotification(channel);
                CONSTANTS.setCurrentUser(response);
                CONSTANTS.setMYORDEROFFSET(0);
                CONSTANTS.RetrieveBookListFromDatabaseInitially(login.this, CONSTANTS.getIdLogin(), dialog);    // This is will retrieve the proper offers too
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                String title;
                String message;

                waitDialog.dismiss();
                btnLogin.setText("Login");
                if(fault.getCode().equals("3003")){
                    tvInvalidLogin.setText("Invalid email or password");
                    tvInvalidLogin.setVisibility(View.VISIBLE);
                }
                else if(fault.getCode().equals("3087")){
                    tvInvalidLogin.setText("Please check you mailbox to confirm your email address");
                    tvInvalidLogin.setVisibility(View.VISIBLE);
                }
                else if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog(login.this, title, message, "Okay");
                }

                else {
                    Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();

                }

                Log.i("deviceid", fault.getMessage() + "\t" + fault.getCode());
            }
        }, stayLoggedIn);
    }


    private void registerDeviceForNotification(String channel) {

        // Testing Backendless and FCM Service

        List<String> channels = new ArrayList<String>();
        channels.add( "default" );
        channels.add(channel);
        Backendless.Messaging.registerDevice(channels, new AsyncCallback<DeviceRegistrationResult>() {
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                // Toast.makeText( Login.this, "Device registered!", Toast.LENGTH_LONG).show();
                Log.i("deviceid", "Device registered successfully on backendless" + "\tTo String: " + response.toString());


            }

            @Override
            public void handleFault(BackendlessFault fault) {
                String title;
                String message;
                if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog(login.this, title, message, "Okay");
                }
                else{
                Toast.makeText( login.this, "Error registering " + fault.getMessage(),
                        Toast.LENGTH_LONG).show();
                }

                Log.i("deviceid", fault.getMessage());
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
        tvInvalidLogin = findViewById(R.id.tvLogin_invalidLogin);
    }
}