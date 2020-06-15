package com.example.gronthomongol.ui.auth.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.gronthomongol.R;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {


    private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        findViews();
        setUpListeners();
        openSignIn();
    }

    public void findViews(){

    }

    public void setUpListeners(){

    }

    public void openSignIn(){
        FragmentTransaction formTransaction = getSupportFragmentManager().beginTransaction();
        formTransaction.add(R.id.fragmentContainerAuth, new SignInFragment(), "signIn");
        formTransaction.commit();
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public void onBackPressed() {
        SignInFragment signInFragment = (SignInFragment) getSupportFragmentManager().findFragmentByTag("signIn");
        if (signInFragment != null && signInFragment.isVisible()) {
            if (doubleBackToExitPressedOnce) {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            } else{
                this.doubleBackToExitPressedOnce = true;
                showToast("Press Once Again to EXIT");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
        }

        SignUpFragment signUpFragment = (SignUpFragment) getSupportFragmentManager().findFragmentByTag("signUp");
        if (signUpFragment != null && signUpFragment.isVisible()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            // Right Swipe Animation
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.fragmentContainerAuth, new SignInFragment(), "signIn");
            fragmentTransaction.commit();
        }


        ForgotPasswordFragment forgotPasswordFragment = (ForgotPasswordFragment) getSupportFragmentManager().findFragmentByTag("forgotPass");
        if (forgotPasswordFragment != null && forgotPasswordFragment.isVisible()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            // Right Swipe Animation
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.fragmentContainerAuth, new SignInFragment(), "signIn");
            fragmentTransaction.commit();
        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
