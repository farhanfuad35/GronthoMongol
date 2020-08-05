package com.example.gronthomongol.ui.main.user.archive;

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
import com.example.gronthomongol.ui.main.user.activity.RequestBookActivity;
import com.example.gronthomongol.ui.util.adapters.UserBooksAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.BackendlessAPIMethods;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.CONSTANTS;

import java.util.List;

public class BooklistActivity extends AppCompatActivity implements UserBooksAdapter.OnBookClickListener {
    private UserBooksAdapter booklistAdapterRV;
    private Button btnProfile;
    private Button btnRequest;
    private Button btnOrders;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;
    private EventHandler<Book> bookEventHandler = Backendless.Data.of(Book.class).rt();
    private EventHandler<Order> orderEventHandler = Backendless.Data.of(Order.class).rt();

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
        handleIntent(getIntent());
        initializeGUIElements();
        initializeRecyclerView();   // Check this for endless scroll data retrieval

        pref = getSharedPreferences("preferences", 0); // 0 - for private mode

        //Toast.makeText(getApplicationContext(), "DEBUG MODE", Toast.LENGTH_SHORT).show();

        fromActivityID = getIntent().getIntExtra(getString(R.string.activityIDName), 0);

        // If has come here just to add more book to the checkout list
        if (fromActivityID == CONSTANTS.getIdPlaceOrderAddMoreBook()) {
            btnRequest.setVisibility(View.GONE);
            btnOrders.setVisibility(View.GONE);

            Log.i(TAG, "onCreate: came here to add more books");

            if(!CONSTANTS.isShowingDefaultBooklist()){
                // Fixed showing of query result
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                booklistAdapterRV.notifyDataSetChanged();
                CONSTANTS.setShowingDefaultBooklist(true);

                Log.i(TAG, "onCreate: showing the query result and fixed the list");
                Log.i(TAG, String.format("booklistCached size: %d\ttempBookListCached Size; %d", CONSTANTS.bookListCached.size(), CONSTANTS.tempBookListCached.size()));
            }
        } else {

            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RequestBookActivity.class);
                    startActivity(intent);
                }
            });
            
            btnOrders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: starting viewOrders");
                    Intent intent = new Intent(BooklistActivity.this, ViewOrdersActivity.class);
                    intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
                    startActivity(intent);
            }
            });

        }

        initiateRealTimeDatabaseListeners();
        initiateRealTimeDatabaseListenersOrders();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        setIntent(intent);
        Log.i("search", "onNewIntent: ");
    }

    public void initiateRealTimeDatabaseListenersOrders() {
        // Update Listener
        Log.i(TAG, "initiateRealTimeDatabaseListeners: update Listener initiated");
        String whereClause = "Recipient_Email = '" + CONSTANTS.getCurrentUser().getEmail() + "'";
        orderEventHandler.addUpdateListener(whereClause, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order updatedOrder) {
                Log.i(TAG, "handleResponse: update listener triggered");
                for (int i = 0; i < CONSTANTS.myOrdersCached.size(); i++) {
                    if (CONSTANTS.myOrdersCached.get(i).equals(updatedOrder)) {
                        CONSTANTS.myOrdersCached.remove(i);
                        CONSTANTS.myOrdersCached.add(i, updatedOrder);
                        if(CONSTANTS.orderlistAdapterRV!=null){
                            CONSTANTS.orderlistAdapterRV.notifyDataSetChanged();
                        }
                    }
                }
                Log.i(TAG, "an Order object has been updated. Object ID - " + updatedOrder.getObjectId());

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Server reported an error while Updating book listener " + fault.getDetail());
            }
        });
    }

    private void handleIntent(Intent intent) {
        Log.i("search", "handleIntent: ");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            //use the query to search your data somehow
            Log.i("search", "came to search. query: " + query);
            if(query.length() < 2){
                Toast.makeText(BooklistActivity.this, "Search query is too small", Toast.LENGTH_SHORT).show();
            }
            else {
                final Dialog waitDialog = new Dialog(BooklistActivity.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_searching_books);
                waitDialog.show();

                final String whereClause = "name LIKE '" + query + "%' OR writer LIKE '" + query + "%' AND quantity > 0";

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.backendlessBookQuery(BooklistActivity.this, waitDialog, whereClause, booklistAdapterRV);
                    }
                });

                thread.start();
                recyclerView.requestFocus();
            }
        }
    }

    public void initiateRealTimeDatabaseListeners() {
        // Update Listener
        bookEventHandler.addUpdateListener(new AsyncCallback<Book>() {
            @Override
            public void handleResponse(Book updatedBook) {
                for(int i=0; i<CONSTANTS.bookListCached.size(); i++){
                    if(CONSTANTS.bookListCached.get(i).equals(updatedBook)){
                        CONSTANTS.bookListCached.remove(i);
                        if(updatedBook.getQuantity()>0) {
                            CONSTANTS.bookListCached.add(i, updatedBook);
                            booklistAdapterRV.notifyDataSetChanged();
                        }
                        break;
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

                final Dialog waitDialog = new Dialog(BooklistActivity.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_please_wait);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.freshRetrieveFromDatabase(BooklistActivity.this, booklistAdapterRV, pref.getString("sortBy", "name"), waitDialog, recyclerView, endlessScrollEventListener);

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
        final MenuItem searchItem = menu.findItem(R.id.menuMain_searchBook);
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
                    booklistAdapterRV.notifyDataSetChanged();
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
            final Dialog dialog = new Dialog(BooklistActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_signing_out);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    BackendlessUser user = Backendless.UserService.CurrentUser();
                    // BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");
                    BackendlessAPIMethods.logOut(BooklistActivity.this, dialog);
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
        btnProfile = findViewById(R.id.btnBookList_Profile);
        btnRequest = findViewById(R.id.btnBookList_RequestBook);
        btnOrders = findViewById(R.id.btnBookList_Orders);
        recyclerView = findViewById(R.id.rvBookList_BookList);
        progressBar = findViewById(R.id.pbBooklist_progressBar);
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        booklistAdapterRV = new UserBooksAdapter(CONSTANTS.bookListCached, getApplicationContext(), this);
        recyclerView.setAdapter(booklistAdapterRV);

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
//            ArrayList<Book> orderedBookList;    // If this fails, use the line above
//            orderedBookList = (ArrayList<Book>) getIntent().getSerializableExtra(getString(R.string.orderedBookList));
            boolean goodToGo = true;


            if (!CONSTANTS.orderedBooks.isEmpty()) {

//                Log.i(TAG, "onBookClick: orderedBookList size: " + orderedBookList.size() + "\tordered book list name: " + orderedBookList.get(0).getName());
//                Log.i(TAG, "onBookClick: clicked Book position: " + position + "\tname: " + CONSTANTS.bookListCached.get(position).getName());

                if (CONSTANTS.orderedBooks.contains(CONSTANTS.bookListCached.get(position))) {
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
        } else if (CONSTANTS.canOrder && CONSTANTS.CURRENT_NUMBER_OF_ORDERS < CONSTANTS.maximum_order_per_day) {
            Intent intent = new Intent(this, PlaceOrderActivity.class);
            intent.putExtra("selectedBook", CONSTANTS.bookListCached.get(position));
//            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
//            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
            Log.i(TAG, "onBookClick: Book: " + CONSTANTS.bookListCached.get(position));
            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
        }

        else {
            // Meaning can't order
            if(!CONSTANTS.canOrder)
                CONSTANTS.showErrorDialog(BooklistActivity.this, "Can't Place Order", CONSTANTS.cannot_order_message, "Okay", null, 0);
            else
                CONSTANTS.showErrorDialog(BooklistActivity.this, "Can't Place Order", "Daily order limit exceeded. Please try again tomorrow", "Okay", null, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_ORDER_RETURN_REQUEST_CODE){
            Log.i(TAG, "onActivityResult: came here");
            if(!CONSTANTS.isShowingDefaultBooklist()) {
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                CONSTANTS.setShowingDefaultBooklist(true);
                booklistAdapterRV.notifyDataSetChanged();
            }
        }
    }

    private void showSortByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BooklistActivity.this);
        builder.setTitle("Sort By")

            .setItems(R.array.sortByArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                final String sortBy;
                if (which == 0)
                    sortBy = "name";
                else
                    sortBy = "writer";
                Log.i("booklist_retrieve", "booklist: sortBy = " + sortBy);

                final Dialog waitDialog = new Dialog(BooklistActivity.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_please_wait);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                            CONSTANTS.freshRetrieveFromDatabase(BooklistActivity.this, booklistAdapterRV, sortBy, waitDialog, recyclerView, endlessScrollEventListener);

                    }
                });

                thread.start();
                }
            });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}