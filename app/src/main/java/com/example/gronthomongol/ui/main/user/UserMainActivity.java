package com.example.gronthomongol.ui.main.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gronthomongol.R;
import com.example.gronthomongol.ui.main.user.archive.RequestBookActivity;
import com.example.gronthomongol.ui.main.user.archive.ViewOrdersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class UserMainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private Button menuButton;
    private Button ordersButton;
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
        ordersButton = (Button) findViewById(R.id.ordersButtonToolbarUserMain);

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
        ordersButton.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
    }

    public void initializeUI(){
//        navigationView.getMenu().findItem(R.id.home_option).setCheckable(true);
//        navigationView.getMenu().findItem(R.id.home_option).setChecked(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerUserMain, new BengaliBooklistFragment()).commit();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch(menuItem.getItemId()){
                case R.id.bengaliTab:
                    selectedFragment = new BengaliBooklistFragment();
                    break;
                case R.id.englishTab:
                    selectedFragment = new EnglishBooklistFragment();
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
        } else if(view == ordersButton){
            new CountDownTimer(100, 20){
                int i;
                @Override
                public void onTick(long l) {
                    if(i%2==0) {
                        ordersButton.setVisibility(View.INVISIBLE);
                    }
                    else{
                        ordersButton.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @Override
                public void onFinish() {
                    ordersButton.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(getApplicationContext(), ViewOrdersActivity.class);
                    startActivity(intent);
                }
            }.start();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.donateDrawerMenuUser) {
            Intent intent = new Intent(getApplicationContext(), DonateActivity.class);
            startActivity(intent);
        } else if (id == R.id.ordersDrawerMenuUser) {
            Intent intent = new Intent(getApplicationContext(), ViewOrdersActivity.class);
            startActivity(intent);
        } else if (id == R.id.requestsDrawerMenuUser) {
            Intent intent = new Intent(getApplicationContext(), RequestBookActivity.class);
            startActivity(intent);
        } else if (id == R.id.feedbackDrawerMenuUser) {
            Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
            startActivity(intent);
        } else if (id == R.id.aboutUsDrawerMenuUser) {
            Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.signOutDrawerMenuUser) {
//            Intent intent = new Intent(getApplicationContext(), ViewOrdersActivity.class);
//            startActivity(intent);
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