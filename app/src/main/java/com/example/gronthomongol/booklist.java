package com.example.gronthomongol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.List;

public class booklist extends AppCompatActivity {

    private BooklistAdapter booklistAdapter;
    private Button btnProfile;
    private Button btnRequest;
    private Button btnOrders;
    private ListView listView;

    int OFFSET = 0;
    int PAGE_SIZE = 8;


    @SuppressLint("ResourceAsColor")
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
        listView = findViewById(R.id.lvBookList_BookList);


        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), request.class);
                startActivity(intent);
            }
        });


        // TODO: FIGURE OUT HOW TO RETRIEVE HUGE DATASET OF BOOKS FROM BACKENDLESS

        RetrieveBookListFromDatabase();
//        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
//        String whereClause = "quantity > 0";
//        queryBuilder.setWhereClause(whereClause);
//        queryBuilder.setPageSize( PAGE_SIZE ).setOffset( OFFSET );
//        queryBuilder.addAllProperties();
//        List<Book> result = Backendless.Data.of( Book.class ).find( queryBuilder );
//        booklistAdapter = new BooklistAdapter(getApplicationContext(), result);
//        listView.setAdapter(booklistAdapter);

    }

    private void RetrieveBookListFromDatabase(){
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        String whereClause = "quantity > 0";
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy("name");
        queryBuilder.setPageSize( PAGE_SIZE ).setOffset( OFFSET );
        Backendless.Data.of( Book.class ).find( queryBuilder,
                new AsyncCallback<List<Book>>()
                {
                    @Override
                    public void handleResponse( List<Book> response )
                    {
                        booklistAdapter = new BooklistAdapter(getApplicationContext(), response);
                        listView.setAdapter(booklistAdapter);

                        Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                        OFFSET+=OFFSET;
                    }

                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        Log.i("booklist_retrieve", "Retrieve Failed : " + fault.getMessage());
                        // use the getCode(), getMessage() or getDetail() on the fault object
                        // to see the details of the error
                    }
                });


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