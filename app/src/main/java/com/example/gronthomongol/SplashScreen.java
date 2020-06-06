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
        CONSTANTS.setOFFSET(0);


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

                RetrieveBookListFromDatabase();

                checkLoginStatus();
            }
        });

        thread.start();
    }

    private void RetrieveBookListFromDatabase(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("preferences", 0); // 0 - for private mode


        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        String whereClause = "quantity > 0";
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy(pref.getString("sortBy", "name"));
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of( Book.class ).find( queryBuilder,
                new AsyncCallback<List<Book>>()
                {
                    @Override
                    public void handleResponse( List<Book> response )
                    {
//                        booklistAdapter = new BooklistAdapter(getApplicationContext(), response);
//                        listView.setAdapter(booklistAdapter);

                        CONSTANTS.bookListCached = response;
                        CONSTANTS.setBookListQueryBuilder(queryBuilder);    // To make this exact querybuilder accessible from all over the app


                        // CHECK LOGIN

                        String userToken = UserTokenStorageFactory.instance().getStorage().get();

                        if( userToken != null && !userToken.equals( "" ) )
                        {
                            final String currentUserId = Backendless.UserService.loggedInUser();

                            Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    Backendless.UserService.setCurrentUser( response );
                                    CONSTANTS.setCurrentUser(response);

                                    // TODO retrieve myOrders here
                                    if(!(boolean)CONSTANTS.getCurrentUser().getProperty("admin")){
                                        CONSTANTS.setMYORDEROFFSET(0);

                                        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                                        final String whereClause = "user.email = '" + CONSTANTS.getCurrentUser().getEmail() + "'";
                                        queryBuilder.setWhereClause(whereClause);
                                        queryBuilder.addAllProperties();
                                        queryBuilder.setSortBy("created DESC");
                                        queryBuilder.setPageSize( CONSTANTS.getMyOrderPageSize() ).setOffset( CONSTANTS.getMYORDEROFFSET() );
                                        Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
                                            @Override
                                            public void handleResponse(List<Order> response) {
                                                //Log.i("myOrders_retrieve", "SplashScreen/handleResponse: where Clause: " + whereClause);
                                                Log.i("myOrders_retrieve", "SplashScreen/handleResponse: My orders retrieved. response size = " + response.size());
                                                CONSTANTS.setMyOrdersCached(response);
                                                CONSTANTS.setOrderListQueryBuilder(queryBuilder);
                                                CONSTANTS.setMYORDEROFFSET(CONSTANTS.getMYORDEROFFSET() + CONSTANTS.getMyOrderPageSize());

                                                // Get out of splash screen & proceed to book list
                                                Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                                                intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdSpalshScreen());
                                                startActivity(intent);
                                                SplashScreen.this.finish();
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                Log.i("myOrders_retrieve", "handleFault: " + fault.getMessage());
                                            }
                                        });
                                    }
                                    // If user is an admin, do not load myOffers
                                    else {
                                        Intent intent = new Intent(getApplicationContext(), com.example.gronthomongol.booklist.class);
                                        intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdSpalshScreen());
                                        startActivity(intent);
                                        SplashScreen.this.finish();
                                    }

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


                        Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                        CONSTANTS.setOFFSET(CONSTANTS.getOFFSET()+CONSTANTS.getPageSize());
                    }

                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                            showConnectionFailedDialog();
                            Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                        }

                        else{
                            Backendless.UserService.logout(new AsyncCallback<Void>() {
                                @Override
                                public void handleResponse(Void response) {
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                                    Log.i("logout", "logout failed");
                                }
                            });
                        }

                        Log.e("fault", fault.getMessage());

                        //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();


                    }


                });


    }

    private void showConnectionFailedDialog()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashScreen.this);
        alertDialogBuilder.setTitle("Connection Failed!");
        alertDialogBuilder.setMessage("Please check your internet connection and try again");
        alertDialogBuilder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        arg0.dismiss();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void checkLoginStatus(){


    }
}