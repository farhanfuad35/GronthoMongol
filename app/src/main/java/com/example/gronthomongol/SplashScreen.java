package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserTokenStorageFactory;

import java.util.List;

public class SplashScreen extends AppCompatActivity {

    ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        setStatusbarColor();
        CONSTANTS.setOFFSET(0);     // This is explicitly the booklist offset


        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.black));

        splashImage = findViewById(R.id.ivSplashScreen_pasheAchiLogo);

        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        splashImage.startAnimation(anim);


        runWaitingThread();
    }


    private void setStatusbarColor()
    {
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
    }

    private void runWaitingThread()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //RetrieveBookListFromDatabase();
                // TODO
                CONSTANTS.getConfigFile(SplashScreen.this, CONSTANTS.getIdSpalshScreen());
                //CONSTANTS.checkLoginStatus(SplashScreen.this, CONSTANTS.getIdSpalshScreen());
            }
        });

        thread.start();
    }
}