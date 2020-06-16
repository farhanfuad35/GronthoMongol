package com.example.gronthomongol.ui.main.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gronthomongol.R;
import com.example.gronthomongol.ui.main.user.UserAboutUsActivity;
import com.example.gronthomongol.ui.main.user.BengaliBooksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AdminMainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

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
        setContentView(R.layout.activity_admin_main);

        setUI();
    }

    void setUI(){
        findXmlElements();
        setUpListeners();
        initializeUI();
    }

    public void findXmlElements(){
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutAdminMain);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarAdminMain);
        menuButton = (Button) findViewById(R.id.menuButtonToolbarAdminMain);

        // Navigation Drawer
        navigationView = (NavigationView) findViewById(R.id.navigationViewAdminMain);
        profileNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nameTextViewDrawerHeader);
        profileEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.emailTextViewDrawerHeader);

        // Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationAdminMain);
    }

    public void setUpListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        menuButton.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
    }

    public void initializeUI(){
//        navigationView.getMenu().findItem(R.id.home_option).setCheckable(true);
//        navigationView.getMenu().findItem(R.id.home_option).setChecked(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerAdminMain, new BengaliBooksFragment()).commit();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch(menuItem.getItemId()){
                case R.id.booksTab:
                    selectedFragment = new BooksFragment();
                    break;
                case R.id.ordersTab:
                    selectedFragment = new AdminOrdersFragment();
                    break;
                case R.id.requestsTab:
                    selectedFragment = new RequestsFragment();
                    break;
                case R.id.bookAddTab:
                    selectedFragment = new BookAddFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerAdminMain, selectedFragment).commit();
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
        if (id == R.id.donateDrawerMenuAdmin) {
            Intent intent = new Intent(getApplicationContext(), DonateActivity.class);
            startActivity(intent);
        } else if (id == R.id.feedbackDrawerMenuAdmin) {
            Intent intent = new Intent(getApplicationContext(), AdminFeedbackActivity.class);
            startActivity(intent);
        } else if (id == R.id.aboutUsDrawerMenuAdmin) {
            Intent intent = new Intent(getApplicationContext(), AdminAboutUsActivity.class);
            startActivity(intent);
        } else if (id == R.id.signOutDrawerMenuAdmin) {
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