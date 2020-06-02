package com.example.gronthomongol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class booklist extends AppCompatActivity {

    private BooklistAdapter booklistAdapter;
    private Button btnProfile;
    private Button btnRequest;
    private Button btnOrders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booklist);

        setTitle("Book List");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_BookList);
        setSupportActionBar(toolbar);

        btnProfile = findViewById(R.id.btnBookList_Profile);
        btnRequest = findViewById(R.id.btnBookList_RequestBook);
        btnOrders = findViewById(R.id.btnBookList_Orders);


        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), request.class);
                startActivity(intent);
            }
        });


        // TODO: FIGURE OUT HOW TO RETRIEVE HUGE DATASET OF BOOKS FROM BACKENDLESS

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menuMain_Logout){

            // Updating device ID while logging out to ensure no more notification is sent to that device

            BackendlessUser user = Backendless.UserService.CurrentUser();

            BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");


            BackendlessAPIMethods.logOut(booklist.this);

        }

        return super.onOptionsItemSelected(item);
    }
}