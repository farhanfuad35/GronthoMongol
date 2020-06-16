package com.example.gronthomongol.ui.main.admin.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.BackendlessAPIMethods;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.main.admin.activity.BookDetailsActivity;
import com.example.gronthomongol.ui.util.adapters.AdminBooksAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;

import java.util.List;
import java.util.Objects;

public class BooksFragment extends Fragment implements AdminBooksAdapter.OnBookClickListener{

    private AdminBooksAdapter adminBooksAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;
    private EventHandler<Book> bookEventHandler = Backendless.Data.of(Book.class).rt();
    private EventHandler<Order> orderEventHandler = Backendless.Data.of(Order.class).rt();

    private int fromActivityID;
    private final String TAG = "booklist_admin";
    private final int PLACE_ORDER_RETURN_REQUEST_CODE = 93;
    private SharedPreferences pref;

    public BooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

//        handleIntent(getActivity().getIntent());
        findXmlElements(view);
        setUpRecyclerView();   // Check this for endless scroll data retrieval

        // For orders
        initiateRealTimeDatabaseListenersOrder_admin();

        pref = getContext().getSharedPreferences("preferences", 0); // 0 - for private mode
        initiateRealTimeDatabaseListeners();

        //Toast.makeText(getApplicationContext(), "DEBUG MODE", Toast.LENGTH_SHORT).show();

        fromActivityID = getActivity().getIntent().getIntExtra(getString(R.string.activityIDName), 0);


        return view;
    }

    private void findXmlElements(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBooks);
        progressBar = view.findViewById(R.id.progressBarBooks);
    }


    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), 0));
        adminBooksAdapter = new AdminBooksAdapter(CONSTANTS.bookListCached, getContext(), this);
        recyclerView.setAdapter(adminBooksAdapter);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) recyclerViewLayoutManager) {
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
                                    adminBooksAdapter.notifyDataSetChanged();
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


    private void initiateRealTimeDatabaseListenersOrder_admin() {
        Log.i(TAG, "initiateRealTimeDatabaseListeners: update Listener initiated");
        // update listener
        orderEventHandler.addUpdateListener( new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order updatedOrder) {
                Log.i(TAG, "handleResponse: update listener triggered");
                for(int i=0; i<CONSTANTS.myOrdersCached.size(); i++){
                    if(CONSTANTS.myOrdersCached.get(i).equals(updatedOrder)){
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
                Log.e(TAG, "Server reported an error while Updating order listener " + fault.getDetail());
            }
        });

        // save listener
        orderEventHandler.addCreateListener(new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order response) {
                CONSTANTS.myOrdersCached.add(0, response);
                if(CONSTANTS.orderlistAdapterRV!=null){
                    CONSTANTS.orderlistAdapterRV.notifyDataSetChanged();
                }
                Log.i(TAG, "handleResponse: New order received");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Server reported an error in create order listener " + fault.getDetail());
            }
        });
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        handleIntent(intent);
//        setIntent(intent);          // MIGHT CAUSE ISSUE
//        Log.i("search", "onNewIntent: ");
//    }

    private void handleIntent(Intent intent) {
        Log.i("search", "handleIntent: ");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            //use the query to search your data somehow
            Log.i("search", "came to search. query: " + query);
            if(query.length() < 2){
                Toast.makeText(getContext(), "Search query is too small", Toast.LENGTH_SHORT).show();
            }
            else {
                final Dialog waitDialog = new Dialog(getContext());
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_searching_books);
                waitDialog.show();

                final String whereClause = "name LIKE '" + query + "%' OR writer LIKE '" + query + "%'";

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.backendlessBookQuery(getContext(), waitDialog, whereClause, adminBooksAdapter);
                    }
                });

                thread.start();
                recyclerView.requestFocus();    // So that keyboard doesn't pop open
            }
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

//                final Dialog waitDialog = new Dialog(Objects.requireNonNull(getContext()));
//                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                waitDialog.setCancelable(false);
//                waitDialog.setContentView(R.layout.dialog_please_wait);
//                waitDialog.show();
//
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        CONSTANTS.freshRetrieveFromDatabase(getContext(), adminBooksAdapter, pref.getString("sortBy", "name"), waitDialog, recyclerView, endlessScrollEventListener);
//                    }
//                });
//
//                thread.start();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i("MYAPP", "Server reported an error " + fault.getDetail());
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.options_menu, menu);
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.menuMain_searchBook).getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        EditText txtSearch = ((EditText)searchView.findViewById(androidx.appcompat.R.id.search_src_text));
//        txtSearch.setHintTextColor(Color.LTGRAY);
//        txtSearch.setTextColor(Color.WHITE);
//
//        // Handling on search back pressed
//        MenuItem searchItem = menu.findItem(R.id.menuMain_searchBook);
//        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                if(!CONSTANTS.isShowingDefaultBooklist()){
//                    CONSTANTS.bookListCached.clear();
//                    CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
//                    adminBooksAdapter.notifyDataSetChanged();
//                    CONSTANTS.setShowingDefaultBooklist(true);
//                }
//
//                return true;
//            }
//        });
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.menuMain_Logout) {
//
//            // Updating device ID while logging out to ensure no more notification is sent to that device
//            final Dialog dialog = new Dialog(getContext());
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setCancelable(false);
//            dialog.setContentView(R.layout.dialog_signing_out);
//            dialog.show();
//
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    // BackendlessAPIMethods.updateDeviceId(booklist.this, user, "");
//                    BackendlessAPIMethods.logOut(getContext(), dialog);
//                }
//            });
//
//            thread.start();
//
//        }
//
//        // If sort by is clicked
//        if (id == R.id.menuMain_sortBy) {
//            showSortByDialog();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBookClick(int position) {
        Intent intent = new Intent(getContext(), BookDetailsActivity.class);
        intent.putExtra("selectedBook", CONSTANTS.bookListCached.get(position));
//            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
//            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
        startActivityForResult(intent, CONSTANTS.getIdBooklistadminBookdetails());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if(requestCode == CONSTANTS.getIdBooklistadminBookdetails()){
            Log.i(TAG, "onActivityResult: request code matched");
            if(resultCode == getActivity().RESULT_OK){
                adminBooksAdapter.notifyDataSetChanged();
                Log.i(TAG, "onActivityResult: Data set changed notified");
            }
            if(!CONSTANTS.isShowingDefaultBooklist()) {
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                CONSTANTS.setShowingDefaultBooklist(true);
                adminBooksAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showSortByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort By")
                .setItems(R.array.sortByArray_admin, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String sortBy;
                        if (which == 0)
                            sortBy = "name";
                        else if(which == 1)
                            sortBy = "writer";
                        else
                            sortBy = "quantity";

                        // Search query book number not handled

                        Log.i("booklist_retrieve", "booklist_admin: sortBy = " + sortBy);

                        final Dialog waitDialog = new Dialog(getContext());
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_please_wait);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CONSTANTS.freshRetrieveFromDatabase(getContext(), adminBooksAdapter, sortBy, waitDialog, recyclerView, endlessScrollEventListener);

                            }
                        });

                        thread.start();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}