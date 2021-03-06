package com.example.gronthomongol.ui.auth.archive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText mobileNumberEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private Button signInInsteadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findXmlElements();
        setUpListeners();
    }

    private void findXmlElements(){
        nameEditText = findViewById(R.id.nameEditTextSignUp);
        emailEditText = findViewById(R.id.emailEditTextSignUp);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditTextSignUp);
        passwordEditText = findViewById(R.id.passwordEditTextSignUp);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditTextSignUp);
        signUpButton = findViewById(R.id.signUpButtonSignUp);
        signInInsteadButton = findViewById(R.id.signInButtonSignUp);
    }

    private void setUpListeners(){
        signUpButton.setOnClickListener(this);
        signInInsteadButton.setOnClickListener(this);
    }

    private void handleSignUp(){
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().toLowerCase().trim();
        String mobileNumber = mobileNumberEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        boolean hasError = false;

        if(name.length() < 3){
            hasError = true;
            nameEditText.setError("কমপক্ষে ৩ অক্ষরের হতে হবে");
        }
        if(!isEmailValid(email)) {
            hasError = true;
            emailEditText.setError("সঠিক নয়");
        }
        if(mobileNumber.isEmpty()){
            mobileNumber = CONSTANTS.NULLMARKER;
        }
        if(password.length() < 3) {
            hasError = true;
            passwordEditText.setError("কমপক্ষে ৩ অক্ষরের হতে হবে");
        }
        if(!password.equals(confirmPassword) || confirmPassword.length() < 3){
            hasError = true;
            confirmPasswordEditText.setError("২টি ভিন্ন পাসওয়ার্ড");
        }


        // TODO
        if(!hasError){
            signUpButton.setText("সাইন আপ হচ্ছে...");

            BackendlessUser user = new BackendlessUser();
            user.setProperty("name", name);
            user.setEmail(email);
            user.setProperty("contact_no", mobileNumber);
            user.setPassword(password);

            Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    Toast.makeText(getApplicationContext(), "সাইন আপ সম্পন্ন হয়েছে!\nকনফার্ম করতে আপনার মেইল পাঠানো ভেরিফিকেশন লিংকটি ভিজিট করুন।", Toast.LENGTH_LONG).show();
                    SignUpActivity.this.finish();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    String title;
                    String message;
                    signUpButton.setText("সাইন আপ");
                    Log.i("register", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
                    if(fault.getCode().equals("3033")){
                        Toast.makeText(getApplicationContext(), "দুঃখিত, প্রদত্ত ইমেইলটি অন্য কোনো একাউন্টের সাথে সংযুক্ত।" , Toast.LENGTH_SHORT).show();
                    }
                    else if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                        title = "Connection Failed!";
                        message = "Please Check Your Internet Connection";
                        CONSTANTS.showErrorDialog(SignUpActivity.this, title, message, "Okay", null, 0);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "দুঃখিত, সাইন আপ সম্পন্ন করা যায় নি।\n" + fault.getMessage() , Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onClick(View view) {
        if(view == signUpButton){
            handleSignUp();
        } else if(view == signInInsteadButton){
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        }
    }
}