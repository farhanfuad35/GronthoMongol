package com.example.gronthomongol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class booklist_admin extends AppCompatActivity implements BooklistAdapterRV_admin.OnBookClickListener {
    private BooklistAdapterRV_admin booklistAdapterRV_admin;
    private Button btnProfile;
    private Button btnRequest;
    private Button btnOrders;
    private FloatingActionButton fabAddNewBook;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;
    private EventHandler<Book> bookEventHandler = Backendless.Data.of(Book.class).rt();

    private int fromActivityID;
    private final String TAG = "booklist_admin";
    private final int PLACE_ORDER_RETURN_REQUEST_CODE = 93;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booklist_admin);

        setTitle("Book List");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_BookList_admin);
        setSupportActionBar(toolbar);
        handleIntent(getIntent());
        initializeGUIElements();
        initializeRecyclerView();   // Check this for endless scroll data retrieval
        pref = getSharedPreferences("preferences", 0); // 0 - for private mode
        initiateRealTimeDatabaseListeners();

        //Toast.makeText(getApplicationContext(), "DEBUG MODE", Toast.LENGTH_SHORT).show();

        fromActivityID = getIntent().getIntExtra(getString(R.string.activityIDName), 0);


        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), requestList.class);
                startActivity(intent);
            }
        });

        btnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: starting viewOrders");
                Intent intent = new Intent(booklist_admin.this, viewOrders_admin.class);
                intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklistAdmin());
                startActivity(intent);
            }
        });

        fabAddNewBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(booklist_admin.this, addBook.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        setIntent(intent);          // MIGHT CAUSE ISSUE
        Log.i("search", "onNewIntent: ");
    }

    private void handleIntent(Intent intent) {
        Log.i("search", "handleIntent: ");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            //use the query to search your data somehow
            Log.i("search", "came to search. query: " + query);

            final Dialog waitDialog = new Dialog(booklist_admin.this);
            waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            waitDialog.setCancelable(false);
            waitDialog.setContentView(R.layout.dialog_searching_books);
            waitDialog.show();

            final String whereClause = "name LIKE '" + query + "%' OR writer LIKE '" + query + "%'";

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    CONSTANTS.backendlessBookQuery(booklist_admin.this, waitDialog, whereClause, booklistAdapterRV_admin);
                }
            });

            thread.start();
            recyclerView.requestFocus();    // So that keyboard doesn't pop open
        }
    }


    public void initiateRealTimeDatabaseListeners() {
        // Create Listener
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

                final Dialog waitDialog = new Dialog(booklist_admin.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_please_wait);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.freshRetrieveFromDatabase(booklist_admin.this, booklistAdapterRV_admin, pref.getString("sortBy", "name"), waitDialog, recyclerView, endlessScrollEventListener);

                    }
                });

                thread.start();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i("MYAPP", "Server reported an error " + fault.getDetail());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menuMain_searchBook).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        EditText txtSearch = ((EditText)searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setTextColor(Color.WHITE);

        // Handling on search back pressed
        MenuItem searchItem = menu.findItem(R.id.menuMain_searchBook);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if(!CONSTANTS.isShowingDefaultBooklist()){
                    CONSTANTS.bookListCached.clear();
                    CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                    booklistAdapterRV_admin.notifyDataSetChanged();
                    CONSTANTS.setShowingDefaultBooklist(true);
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuMain_Logout) {

            // Updating device ID while logging out to ensure no more notification is sent to that device
            final Dialog dialog = new Dialog(booklist_admin.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_logging_out);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");
                    BackendlessAPIMethods.logOut(booklist_admin.this, dialog);
                }
            });

            thread.start();

        }

        // If sort by is clicked
        if (id == R.id.menuMain_sortBy) {
            showSortByDialog();

        }


        return super.onOptionsItemSelected(item);
    }

    private void initializeGUIElements() {
        btnProfile = findViewById(R.id.btnBookList_admin_Profile);
        btnRequest = findViewById(R.id.btnBookList_admin_RequestBook);
        btnOrders = findViewById(R.id.btnBookList_admin_Orders);
        fabAddNewBook = findViewById(R.id.fabBookList_admin_addNewBook);
//      listView = findViewById(R.id.lvBookList_BookList);
        recyclerView = findViewById(R.id.rvBookList_admin_BookList);
        progressBar = findViewById(R.id.pbBooklist_admin_progressBar);
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        booklistAdapterRV_admin = new BooklistAdapterRV_admin(CONSTANTS.bookListCached, getApplicationContext(), this);
        recyclerView.setAdapter(booklistAdapterRV_admin);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, RecyclerView recyclerView) {
                if (CONSTANTS.isShowingDefaultBooklist()) {
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
                                    booklistAdapterRV_admin.notifyDataSetChanged();

                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.i("booklist_paging", "new response failed");
                                    // use the getCode(), getMessage() or getDetail() on the fault object
                                    // to see the details of the error
                                }
                            });
                }


            }
        };

        recyclerView.addOnScrollListener(endlessScrollEventListener);
    }

    @Override
    public void onBookClick(int position) {

        Intent intent = new Intent(this, bookDetails.class);
        intent.putExtra("selectedBook", CONSTANTS.bookListCached.get(position));
//            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
//            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
        startActivityForResult(intent, CONSTANTS.getIdBooklistadminBookdetails());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if(requestCode == CONSTANTS.getIdBooklistadminBookdetails()){
            Log.i(TAG, "onActivityResult: request code matched");
            if(resultCode == RESULT_OK){
                booklistAdapterRV_admin.notifyDataSetChanged();
                Log.i(TAG, "onActivityResult: Data set changed notified");
            }
            if(!CONSTANTS.isShowingDefaultBooklist()) {
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                CONSTANTS.setShowingDefaultBooklist(true);
                booklistAdapterRV_admin.notifyDataSetChanged();
            }
        }
    }

    private void showSortByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(booklist_admin.this);
        builder.setTitle("Sort By")

                .setItems(R.array.sortByArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String sortBy;
                        if (which == 0)
                            sortBy = "name";
                        else
                            sortBy = "writer";
                        Log.i("booklist_retrieve", "booklist_admin: sortBy = " + sortBy);

                        final Dialog waitDialog = new Dialog(booklist_admin.this);
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_please_wait);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CONSTANTS.freshRetrieveFromDatabase(booklist_admin.this, booklistAdapterRV_admin, sortBy, waitDialog, recyclerView, endlessScrollEventListener);

                            }
                        });

                        thread.start();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}