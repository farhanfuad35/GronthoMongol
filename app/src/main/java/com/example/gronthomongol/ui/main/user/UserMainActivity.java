package com.example.gronthomongol.ui.main.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.BackendlessAPIMethods;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.ui.main.admin.AdminMainActivity;
import com.example.gronthomongol.ui.main.user.activity.RequestBookActivity;
import com.example.gronthomongol.ui.main.user.fragment.BagFragment;
import com.example.gronthomongol.ui.main.user.fragment.BengaliBooksFragment;
import com.example.gronthomongol.ui.main.user.fragment.EnglishBooksFragment;
import com.example.gronthomongol.ui.main.user.fragment.UserOrdersFragment;
import com.example.gronthomongol.ui.main.user.minor.UserAboutUsActivity;
import com.example.gronthomongol.ui.main.user.minor.UserDonateActivity;
import com.example.gronthomongol.ui.main.user.minor.UserFeedbackActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class UserMainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private Button menuButton;
    private NavigationView navigationView;

    private TextView profileNameTextView;
    private TextView profileEmailTextView;

    private ActionBarDrawerToggle drawerToggle;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        setUI();
    }

    void setUI(){
        findXmlElements();
        setUpListeners();
        initializeUI();
    }

    public void findXmlElements(){
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutUserMain);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarUserMain);
        menuButton = (Button) findViewById(R.id.menuButtonToolbarUserMain);

        // Navigation Drawer
        navigationView = (NavigationView) findViewById(R.id.navigationViewUserMain);
        profileNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nameTextViewDrawerHeader);
        profileEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.emailTextViewDrawerHeader);

        // Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationUserMain);
    }

    public void setUpListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        menuButton.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
    }

    public void initializeUI(){
        updateNavigationView();
        navigationView.getMenu().findItem(R.id.homeDrawerMenuUser).setChecked(true);
        profileNameTextView.setText(CONSTANTS.getCurrentUser().getProperty("name").toString());
        profileEmailTextView.setText(CONSTANTS.getCurrentUser().getEmail());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerUserMain, new BengaliBooksFragment()).commit();
    }

    private void updateNavigationView(){
        navigationView.getMenu().findItem(R.id.homeDrawerMenuUser).setChecked(false);
        navigationView.getMenu().findItem(R.id.donateDrawerMenuUser).setChecked(false);
        navigationView.getMenu().findItem(R.id.feedbackDrawerMenuUser).setChecked(false);
        navigationView.getMenu().findItem(R.id.aboutUsDrawerMenuUser).setChecked(false);
        navigationView.getMenu().findItem(R.id.signOutDrawerMenuUser).setChecked(false);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch(menuItem.getItemId()){
                case R.id.bengaliTab:
                    selectedFragment = new BengaliBooksFragment();
                    break;
                case R.id.englishTab:
                    selectedFragment = new EnglishBooksFragment();
                    break;
                case R.id.bagTab:
                    selectedFragment = new BagFragment();
                    break;
                case R.id.ordersTab:
                    selectedFragment = new UserOrdersFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerUserMain, selectedFragment).commit();
            return true;
        }
    };

    @Override
    public void onClick(View view) {
        if(view == menuButton){
            new CountDownTimer(100, 20){
                int i;
                @Override
                public void onTick(long l) {
                    if(i%2==0) {
                        menuButton.setVisibility(View.INVISIBLE);
                    }
                    else{
                        menuButton.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @Override
                public void onFinish() {
                    menuButton.setVisibility(View.VISIBLE);
                    if(drawerLayout.isDrawerOpen(navigationView)){
                        drawerLayout.closeDrawer(navigationView);
                    }

                    else if(!drawerLayout.isDrawerOpen(navigationView)){
                        drawerLayout.openDrawer(navigationView);
                    }
                }
            }.start();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.homeDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.homeDrawerMenuUser).setChecked(true);
            Intent intent = new Intent(getApplicationContext(), UserMainActivity.class);
            startActivity(intent);
        } else if (id == R.id.requestsDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.requestsDrawerMenuUser).setChecked(true);
            Intent intent = new Intent(getApplicationContext(), RequestBookActivity.class);
            startActivity(intent);
        } else if(id == R.id.donateDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.donateDrawerMenuUser).setChecked(true);
            Intent intent = new Intent(getApplicationContext(), UserDonateActivity.class);
            startActivity(intent);
        } else if (id == R.id.feedbackDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.feedbackDrawerMenuUser).setChecked(true);
            Intent intent = new Intent(getApplicationContext(), UserFeedbackActivity.class);
            startActivity(intent);
        } else if (id == R.id.aboutUsDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.aboutUsDrawerMenuUser).setChecked(true);
            Intent intent = new Intent(getApplicationContext(), UserAboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.signOutDrawerMenuUser) {
            updateNavigationView();
            navigationView.getMenu().findItem(R.id.signOutDrawerMenuUser).setChecked(true);
            // Updating device ID while logging out to ensure no more notification is sent to that device
            final Dialog dialog = new Dialog(UserMainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_signing_out);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");
                    BackendlessAPIMethods.logOut(UserMainActivity.this, dialog);
                }
            });

            thread.start();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showToast(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}