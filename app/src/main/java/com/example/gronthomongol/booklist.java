package com.example.gronthomongol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class booklist extends AppCompatActivity implements BooklistAdapterRV.OnBookClickListener {
    private BooklistAdapterRV booklistAdapterRV;
    private Button btnProfile;
    private Button btnRequest;
    private Button btnOrders;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;
    private EventHandler<Book> bookEventHandler = Backendless.Data.of(Book.class).rt();

    private int fromActivityID;
    private final String TAG = "booklist";
    private final int PLACE_ORDER_RETURN_REQUEST_CODE = 97;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booklist);

        setTitle("Book List");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_BookList);
        setSupportActionBar(toolbar);
        initializeGUIElements();
        initializeRecyclerView();   // Check this for endless scroll data retrieval
        pref = getSharedPreferences("preferences", 0); // 0 - for private mode

        fromActivityID = getIntent().getIntExtra(getString(R.string.activityIDName), 0);

        // If has come here just to add more book to the checkout list
        if (fromActivityID == CONSTANTS.getIdPlaceOrderAddMoreBook()) {
            btnRequest.setVisibility(View.GONE);
            btnOrders.setVisibility(View.GONE);
        } else {

            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), postRequest.class);
                    startActivity(intent);
                }
            });
            
            btnOrders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if((boolean)CONSTANTS.getCurrentUser().getProperty("admin")){
                        
                    }
                    else{
                        Log.i(TAG, "onClick: starting viewOrders");
                        Intent intent = new Intent(booklist.this, viewOrders.class);
                        intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
                        startActivity(intent);
                    }
                }
            });

        }

        initiateRealTimeDatabaseListeners();


    }


    public void initiateRealTimeDatabaseListeners() {
        // Update Listener
        bookEventHandler.addUpdateListener(new AsyncCallback<Book>() {
            @Override
            public void handleResponse(Book updatedBook) {
                for(int i=0; i<CONSTANTS.bookListCached.size(); i++){
                    if(CONSTANTS.bookListCached.get(i).equals(updatedBook)){
                        CONSTANTS.bookListCached.remove(i);
                        CONSTANTS.bookListCached.add(updatedBook);
                        booklistAdapterRV.notifyDataSetChanged();
                    }
                }
                Log.i(TAG, "an Order object has been updated. Object ID - " + updatedBook.getObjectId());

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Server reported an error while Updating book listener " + fault.getDetail());
            }
        });

        // Delete Listener

        bookEventHandler.addDeleteListener(new AsyncCallback<Book>() {
            @Override
            public void handleResponse(Book deletedBook) {
                Log.i(TAG, "an Order object has been deleted. Object ID - " + deletedBook.getObjectId());
                if (CONSTANTS.bookListCached.contains(deletedBook)) {
                    CONSTANTS.bookListCached.remove(deletedBook);
                    booklistAdapterRV.notifyDataSetChanged();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Server reported an error " + fault.getDetail());
            }
        });

        bookEventHandler.addCreateListener(new AsyncCallback<Book>() {
            @Override
            public void handleResponse(Book createdBook) {

                Log.i(TAG, "new Order object has been created. Object ID - " + createdBook.getObjectId());
//                CONSTANTS.bookListCached.add(createdBook);
//                Collections.sort(CONSTANTS.bookListCached, new SortbyName());
//                booklistAdapterRV.notifyDataSetChanged();

                // TODO CALL A FRESH REQUEST
                // Because just adding causes problem with the offset. Because you don't know if the newly added book is supposed to be on the
                // cached booklist or not because of the sorting. So better do a fresh retrieve

                final Dialog waitDialog = new Dialog(booklist.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_please_wait);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.freshRetrieveFromDatabase(booklist.this, booklistAdapterRV, pref.getString("sortBy", "name"), waitDialog, recyclerView, endlessScrollEventListener);

                    }
                });

                thread.start();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("MYAPP", "Server reported an error " + fault.getDetail());
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

        if (id == R.id.menuMain_Logout) {

            // Updating device ID while logging out to ensure no more notification is sent to that device

            BackendlessUser user = Backendless.UserService.CurrentUser();

            BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");


            BackendlessAPIMethods.logOut(booklist.this);

        }

        // If sort by is clicked
        if (id == R.id.menuMain_sortBy) {
            showSortByDialog();

        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeGUIElements() {
        btnProfile = findViewById(R.id.btnBookList_Profile);
        btnRequest = findViewById(R.id.btnBookList_RequestBook);
        btnOrders = findViewById(R.id.btnBookList_Orders);
//      listView = findViewById(R.id.lvBookList_BookList);
        recyclerView = findViewById(R.id.rvBookList_BookList);
        progressBar = findViewById(R.id.pbBooklist_progressBar);
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        booklistAdapterRV = new BooklistAdapterRV(CONSTANTS.bookListCached, getApplicationContext(), this);
        recyclerView.setAdapter(booklistAdapterRV);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, RecyclerView recyclerView) {

                progressBar.setVisibility(View.VISIBLE);

                DataQueryBuilder queryBuilder = CONSTANTS.getBookListQueryBuilder();
                queryBuilder.prepareNextPage();

                Log.i(TAG, "onLoadMore: Came to OnloadMore");

                Backendless.Data.of(Book.class).find(queryBuilder,
                        new AsyncCallback<List<Book>>() {
                            @Override
                            public void handleResponse(List<Book> response) {
                                Log.i("booklist_paging", "new response received");

                                CONSTANTS.bookListCached.addAll(response);
                                progressBar.setVisibility(View.GONE);
                                booklistAdapterRV.notifyDataSetChanged();

                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.i("booklist_paging", "new response failed");
                                // use the getCode(), getMessage() or getDetail() on the fault object
                                // to see the details of the error
                            }
                        });


            }
        };

        recyclerView.addOnScrollListener(endlessScrollEventListener);
    }

    @Override
    public void onBookClick(int position) {

        // Came here just to add more book to the checkout items
        if (fromActivityID == CONSTANTS.getIdPlaceOrderAddMoreBook()) {

            Log.i(TAG, "onBookClick: Came here to add more books");

            // ArrayList<Book> orderedBookList =  new ArrayList<>();
            ArrayList<Book> orderedBookList;    // If this fails, use the line above
            orderedBookList = (ArrayList<Book>) getIntent().getSerializableExtra(getString(R.string.orderedBookList));
            boolean goodToGo = true;


            if (!orderedBookList.isEmpty()) {

//                Log.i(TAG, "onBookClick: orderedBookList size: " + orderedBookList.size() + "\tordered book list name: " + orderedBookList.get(0).getName());
//                Log.i(TAG, "onBookClick: clicked Book position: " + position + "\tname: " + CONSTANTS.bookListCached.get(position).getName());

                if (orderedBookList.contains(CONSTANTS.bookListCached.get(position))) {
                    Toast.makeText(getApplicationContext(), "Book already selected", Toast.LENGTH_LONG).show();
                    goodToGo = false;
                }
            }
            if (goodToGo) {
                Log.i(TAG, "onBookClick: About to return intent");

                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.newlySelectedBook), CONSTANTS.bookListCached.get(position));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        } else {
            Intent intent = new Intent(this, placeOrder.class);
            intent.putExtra("selectedBook", CONSTANTS.bookListCached.get(position));
//            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
//            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
            startActivity(intent);
        }
    }

    private void showSortByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(booklist.this);
        builder.setTitle("Sort By")

                .setItems(R.array.sortByArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String sortBy;
                        if (which == 0)
                            sortBy = "name";
                        else
                            sortBy = "writer";
                        Log.i("booklist_retrieve", "booklist: sortBy = " + sortBy);

                        final Dialog waitDialog = new Dialog(booklist.this);
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_please_wait);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CONSTANTS.freshRetrieveFromDatabase(booklist.this, booklistAdapterRV, sortBy, waitDialog, recyclerView, endlessScrollEventListener);

                            }
                        });

                        thread.start();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}