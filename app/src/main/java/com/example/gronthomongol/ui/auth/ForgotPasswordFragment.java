package com.example.gronthomongol.ui.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.gronthomongol.R;

public class ForgotPasswordFragment extends Fragment implements View.OnClickListener{

    private Button sendLinkButton;
    private EditText emailEditText;
    private String email;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        findXmlElements(view);
        setUpListeners();

        return view;
    }

    private void findXmlElements(View view){
        sendLinkButton = view.findViewById(R.id.sendLinkButtonForgotPassword);
        emailEditText = view.findViewById(R.id.emailEditTextForgotPassword);
    }

    private void setUpListeners(){
        sendLinkButton.setOnClickListener(this);
    }

    private void handleLinkSend(){
        email = emailEditText.getText().toString().trim();
        if(email.isEmpty()){
            emailEditText.setError("সঠিক নয়");
        }
        else{
            sendLinkButton.setText("লিংক পাঠানো হচ্ছে...");
            Backendless.UserService.restorePassword( email, new AsyncCallback<Void>() {

                public void handleResponse( Void response ) {
                    Log.i("forgot_password", "handleResponse: Password Reset");
                    // Backendless has completed the operation - an email has been sent to the user

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("পাসওয়ার্ড পুনরুদ্ধার");
                    alertDialogBuilder.setMessage("পাসওয়ার্ড পুনরুদ্ধার এর জন্য একটি লিংক প্রদত্ত মেইলে পাঠানো হয়েছে। মেইলটি চেক করুন এবং আপনার পাসওয়ার্ড পুনরুদ্ধার করুন।\n\nধন্যবাদ।\n");
                    alertDialogBuilder.setPositiveButton("ঠিক আছে",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    goToSignIn();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                public void handleFault( BackendlessFault fault ) {
                    sendLinkButton.setText("Reset Password");
                    if(fault.getCode().equals("3020")) {
                        emailEditText.setError("প্রদত্ত ইমেইল এর কোনো ইউজারের সাথে সংযুক্ত নয়");
                    }
                    else{
                        Toast.makeText(getContext(), "দুঃখিত, আবার চেষ্টা করুন!", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }

    private void goToSignIn(){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.fragmentContainerAuth, new SignInFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View view) {
        if(view==sendLinkButton){
            handleLinkSend();
        }
    }
}