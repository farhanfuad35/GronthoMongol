package com.example.gronthomongol.ui.auth.archive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.gronthomongol.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    Button btnResetPassword;
    EditText etEmail;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setTitle("Reset Password");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_ForgotPassword);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnResetPassword = findViewById(R.id.btnResetPassword);
        etEmail = findViewById(R.id.etForgotPass_Email);
        email = etEmail.getText().toString().trim();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString().trim();
                if(email.isEmpty()){
                    etEmail.setError("Please enter your email address");
                }
                else{
                    btnResetPassword.setText("Resetting Password...");
                    Backendless.UserService.restorePassword( email, new AsyncCallback<Void>()
                    {
                        public void handleResponse( Void response )
                        {
                            Log.i("forgot_password", "handleResponse: Password Reset");
                            // Backendless has completed the operation - an email has been sent to the user

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                            alertDialogBuilder.setTitle("Password Reset");
                            alertDialogBuilder.setMessage("An email with password reset link has been sent to your email address. Please check your inbox.");
                            alertDialogBuilder.setPositiveButton("Okay",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }

                        public void handleFault( BackendlessFault fault )
                        {
                            btnResetPassword.setText("Reset Password");
                            if(fault.getCode().equals("3020")) {
                                etEmail.setError("No user found with this email address");
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                            }
                            Log.i("forgot_password", "handleFault: Password Reset Failed. " + fault.getCode() + "\t" + fault.getMessage());
                            // password revovery failed, to get the error code call fault.getCode()
                        }
                    });
                }
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}