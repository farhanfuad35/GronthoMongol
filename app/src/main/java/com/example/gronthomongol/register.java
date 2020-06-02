package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class register extends AppCompatActivity {

    EditText etContactNo;
    EditText etName;
    EditText etEmail;
    EditText etPassWord;
    EditText etConfirmPassword;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        guiElementInitialization();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactNo = etContactNo.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().toLowerCase().trim();
                String password = etPassWord.getText().toString();
                String confirmPass = etConfirmPassword.getText().toString();

                boolean hasError = false;

                if(contactNo.length() != 11){
                    hasError = true;
                    etContactNo.setError("Please type your valid Contact Number");
                }
                if(!isEmailValid(email)) {
                    hasError = true;
                    etEmail.setError("Please enter a valid e-mail address");
                }
                if(password.length() < 3) {
                    hasError = true;
                    etPassWord.setError("Password should be at least 3 characters");
                }
                if(!password.equals(confirmPass)){
                    hasError = true;
                    etConfirmPassword.setError("The passwords don't match");
                }

                // TODO

                if(!hasError){
                    BackendlessUser user = new BackendlessUser();
                    user.setPassword(password);
                    user.setEmail(email);
                    user.setProperty("name", name);
                    user.setProperty("contact_no", contactNo);


                    Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Toast.makeText(getApplicationContext(), "SignUp Successfull!", Toast.LENGTH_SHORT).show();
                            register.this.finish();

                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getApplicationContext(), "SignUp Failed. Please check if you are already registered" , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }


    private void guiElementInitialization(){
        etConfirmPassword = findViewById(R.id.etRegister_confirmpassword);
        etName = findViewById(R.id.etRegister_name);
        etEmail = findViewById(R.id.etRegister_Email);
        etPassWord = findViewById(R.id.etRegister_password);
        etContactNo = findViewById(R.id.etRegister_ContactNumber);
        btnRegister = findViewById(R.id.btnRegister_createAccount);
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}