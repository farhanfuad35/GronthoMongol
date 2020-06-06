package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

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
                        btnLogin.setText("Logging in...");
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
                                Login(email, password, stayLoggedIn);
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


    private void Login(final String email, final String password, Boolean stayLoggedIn)
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

                // If user is not an admin, load my offers
                if(!(boolean)response.getProperty("admin")){
                    final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                    final String whereClause = "user.email = '" + CONSTANTS.getCurrentUser().getEmail().toString().trim() + "'";
                    queryBuilder.setWhereClause(whereClause);
                    queryBuilder.addAllProperties();
                    queryBuilder.setSortBy("created DESC");
                    queryBuilder.setPageSize( CONSTANTS.getMyOrderPageSize() ).setOffset( CONSTANTS.getMYORDEROFFSET() );
                    Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
                        @Override
                        public void handleResponse(List<Order> response) {
                            //Log.i("myOrders_retrieve", "handleResponse: where Clause: " + whereClause);
                            Log.i("myOrders_retrieve", "login/handleResponse: My orders retrieved. response size = " + response.size());
                            CONSTANTS.setMyOrdersCached(response);
                            CONSTANTS.setOrderListQueryBuilder(queryBuilder);
                            CONSTANTS.setMYORDEROFFSET(CONSTANTS.getMYORDEROFFSET() + CONSTANTS.getMyOrderPageSize());

                            // Get out of splash screen & proceed to book list
                            waitDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdLogin());
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            waitDialog.dismiss();
                            if(fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless))){
                                CONSTANTS.showConnectionFailedDialogWithoutRestart(login.this);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Couldn't load user offers", Toast.LENGTH_SHORT).show();
                            }
                            Log.i("myOrders_retrieve", "handleFault: " + fault.getMessage());
                        }
                    });
                }
                // If user is an admin, don't load my offers
                else {
                    waitDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                    intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdLogin());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

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

                else if(fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless))){
                    CONSTANTS.showConnectionFailedDialogWithoutRestart(login.this);
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
                if(fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless))){
                    CONSTANTS.showConnectionFailedDialogWithoutRestart(login.this);
                }
                else{
                Toast.makeText( login.this, "Error registering " + fault.getMessage(),
                        Toast.LENGTH_LONG).show();
                }

                Log.i("deviceid", fault.getMessage());
            }
        });

        // The following Methods were used previously to retrieve the Device registration id and set it
        // to the device_Id column, created manually. of the user table. So that once the user is logged out,
        // it can be set to null. Which previously seemed to be a problem. But I think we can save this trouble
        // if we cancel the device registration following the documentation of backendless
        // Nevermind...
        // Actually do mind. Don't need this.

//        Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
//            @Override
//            public void handleResponse(DeviceRegistration response) {
//
//                BackendlessUser user = Backendless.UserService.CurrentUser();
//                BackendlessAPIMethods.updateDeviceId(login.this, user, response.getDeviceId());
//
//            }
//
//            @Override
//            public void handleFault(BackendlessFault fault) {
//
//            }
//        });

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