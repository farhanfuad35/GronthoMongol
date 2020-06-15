package com.example.gronthomongol.ui.auth;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.push.DeviceRegistrationResult;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;

import java.util.ArrayList;
import java.util.List;

public class SignInFragment extends Fragment implements View.OnClickListener{

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;
    private TextView forgotPasswordButton;
    private TextView errorTextViewSignIn;

    private String email;
    private String password;

    private CheckBox cbStayLoggedIn;

    private Dialog waitDialog;

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        findXmlElements(view);
        setUpListeners();

        return view;
    }

    private void findXmlElements(View view) {
        emailEditText = view.findViewById(R.id.emailEditTextSignIn);
        passwordEditText = view.findViewById(R.id.passwordEditTextSignIn);
        signInButton = view.findViewById(R.id.signInButtonSignIn);
        signUpButton = view.findViewById(R.id.signUpButtonSignIn);
        forgotPasswordButton = view.findViewById(R.id.forgotPasswordButtonSignIn);
        errorTextViewSignIn = view.findViewById(R.id.errorTextViewSignIn);
    }

    private void setUpListeners(){
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
    }

    private void handleSignIn(){
        errorTextViewSignIn.setVisibility(View.GONE);
        if(getValues()) {
            if (!isEmailValid(email)) {
                // can't proceed for loggin in
                //etEmail.setError("Please enter a valid email address");
                errorTextViewSignIn.setText("ইমেইল সঠিক নয়");
                errorTextViewSignIn.setVisibility(View.VISIBLE);
            }

            else {
                final String email = emailEditText.getText().toString().toLowerCase();
                final String password = passwordEditText.getText().toString();
                //final Boolean stayLoggedIn = cbStayLoggedIn.isChecked();
                final Boolean stayLoggedIn = true;

                waitDialog = new Dialog(getContext());
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_signing_in);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SignIn(email, password, stayLoggedIn, waitDialog);
                    }
                });

                thread.start();

//                    BackendlessAPIMethods.Login(getApplicationContext(), email, password, stayLoggedIn, getApplicationContext().getAct);
            }
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Return true if gets values successfully
    private boolean getValues(){
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString();

        if(email.isEmpty()){
//            emailEditText.setError("সঠিক নয়");
            errorTextViewSignIn.setText("ইমেইল সঠিক নয়");
            errorTextViewSignIn.setVisibility(View.VISIBLE);
            return false;
        }
        else if(password.isEmpty()){
//            passwordEditText.setError("সঠিক নয়");
            errorTextViewSignIn.setText("পাসওয়ার্ড সঠিক নয়");
            errorTextViewSignIn.setVisibility(View.VISIBLE);
            return false;
        }
        else
            return true;
    }


    private void SignIn(final String email, final String password, Boolean stayLoggedIn, final Dialog dialog) {
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
                CONSTANTS.RetrieveBookListFromDatabaseInitially(getContext(), CONSTANTS.getIdLogin(), dialog);    // This is will retrieve the proper offers too
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                String title;
                String message;

                waitDialog.dismiss();
                signInButton.setText("সাইন ইন");

                if(fault.getCode().equals("3003")){
                    errorTextViewSignIn.setText("ইমেইল অথবা পাসওয়ার্ড সঠিক নয়");
                    errorTextViewSignIn.setVisibility(View.VISIBLE);
                }
                else if(fault.getCode().equals("3087")){
                    errorTextViewSignIn.setText("আপনার মেইল চেক করুন এবং আপনার ইমেইলটি নিশ্চিত করুন।");
                    errorTextViewSignIn.setVisibility(View.VISIBLE);
                }
                else if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog(getContext(), title, message, "ঠিক আছে", null, 0);
                }

                else {
                    Toast.makeText(getContext(), "দুঃখিত, সাইন ইন সফল হয়নি!", Toast.LENGTH_SHORT).show();
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
                    CONSTANTS.showErrorDialog(getContext(), title, message, "ঠিক আছে", null, 0);
                }
                else{
                    Toast.makeText( getContext(), "Error Registering " + fault.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                Log.i("deviceid", fault.getMessage());
            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view == signInButton){
            handleSignIn();
        } else if(view == signUpButton){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.fragmentContainerAuth, new SignUpFragment(), "signUp");
            fragmentTransaction.commit();
        } else if(view == forgotPasswordButton){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.fragmentContainerAuth, new ForgotPasswordFragment(), "forgotPassword");
            fragmentTransaction.commit();
        }
    }
}