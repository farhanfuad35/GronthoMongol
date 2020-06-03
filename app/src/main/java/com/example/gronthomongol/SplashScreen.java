package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserTokenStorageFactory;

public class SplashScreen extends AppCompatActivity {

    ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        setStatusbarColor();


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

//                callTuitionListFromBackendless();

                checkLoginStatus();
            }
        });

        thread.start();
    }

    private void checkLoginStatus(){

        String userToken = UserTokenStorageFactory.instance().getStorage().get();

        if( userToken != null && !userToken.equals( "" ) )
        {
            String currentUserId = Backendless.UserService.loggedInUser();

            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    Backendless.UserService.setCurrentUser( response );


                    Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                    startActivity(intent);
                    SplashScreen.this.finish();

                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.login.class);
                    startActivity(intent);
                    SplashScreen.this.finish();
                }
            });




        }
        else{
            Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.login.class);
            startActivity(intent);
            SplashScreen.this.finish();
        }
    }
}