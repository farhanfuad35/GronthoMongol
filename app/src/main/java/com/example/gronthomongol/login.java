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

        tvForgetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, forgotPassword.class);
                startActivity(intent);
            }
        });
    }



    private void Login(final String email, final String password, Boolean stayLoggedIn)
    {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {

                // registerDeviceForNotification();         //TODO

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
                            Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdLogin());
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i("myOrders_retrieve", "handleFault: " + fault.getMessage());
                        }
                    });
                }
                // If user is an admin, don't load my offers
                else {
                    Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                    intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdLogin());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                btnLogin.setText("Login");
                if(fault.getCode().equals("3003")){
                    tvInvalidLogin.setVisibility(View.VISIBLE);
                }

                else {
                    Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();

                }

                Log.i("deviceid", fault.getMessage() + "\t" + fault.getCode());
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
        tvInvalidLogin = findViewById(R.id.tvLogin_invalidLogin);
    }
}