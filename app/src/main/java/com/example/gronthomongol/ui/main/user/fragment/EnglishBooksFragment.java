package com.example.gronthomongol.ui.main.user.fragment;

import android.app.Activity;
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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.main.user.archive.PlaceOrderActivity;
import com.example.gronthomongol.ui.util.adapters.UserBooksAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;

import java.util.List;


public class EnglishBooksFragment extends Fragment implements UserBooksAdapter.OnBookClickListener, View.OnClickListener {
    private EditText searchEditText;
    private ImageButton sortImageButton;

    private UserBooksAdapter userBooksAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;

    private EventHandler<Book> bookEventHandler = Backendless.Data.of(Book.class).rt();
    private EventHandler<Order> orderEventHandler = Backendless.Data.of(Order.class).rt();

    private int fromActivityID;
    private final String TAG = "booklist";
    private final int PLACE_ORDER_RETURN_REQUEST_CODE = 97;
    private SharedPreferences pref;

    public EnglishBooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_english_books, container, false);

        //        handleIntent(getIntent());
        findXmlElements(view);
        setUpListeners();
        setUpRecyclerView();   // Check this for endless scroll data retrieval

        pref = getContext().getSharedPreferences("preferences", 0); // 0 - for private mode

        //Toast.makeText(getApplicationContext(), "DEBUG MODE", Toast.LENGTH_SHORT).show();

        fromActivityID = getActivity().getIntent().getIntExtra(getString(R.string.activityIDName), 0);

        // If has come here just to add more book to the checkout list
        if (fromActivityID == CONSTANTS.getIdPlaceOrderAddMoreBook()) {
            Log.i(TAG, "onCreate: came here to add more books");
            if(!CONSTANTS.isShowingDefaultBooklist()){
                // Fixed showing of query result
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                userBooksAdapter.notifyDataSetChanged();
                CONSTANTS.setShowingDefaultBooklist(true);

                Log.i(TAG, "onCreate: showing the query result and fixed the list");
                Log.i(TAG, String.format("booklistCached size: %d\ttempBookListCached Size; %d", CONSTANTS.bookListCached.size(), CONSTANTS.tempBookListCached.size()));
            }
        }

        initiateRealTimeDatabaseListeners();

        return view;
    }

    private void findXmlElements(View view) {
        searchEditText = view.findViewById(R.id.searchEditTextEnglishBooks);
        sortImageButton = view.findViewById(R.id.sortImageButtonEnglishBooks);

        recyclerView = view.findViewById(R.id.recyclerViewEnglishBooks);
        progressBar = view.findViewById(R.id.progressBarEnglishBooks);
    }

    private void setUpListeners(){
        sortImageButton.setOnClickListener(this);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 0));
        userBooksAdapter = new UserBooksAdapter(CONSTANTS.bookListCached, getContext(), this);
        recyclerView.setAdapter(userBooksAdapter);

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
                                    userBooksAdapter.notifyDataSetChanged();
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
                            userBooksAdapter.notifyDataSetChanged();
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
                    userBooksAdapter.notifyDataSetChanged();
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

                final Dialog waitDialog = new Dialog(getContext());
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_please_wait);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.freshRetrieveFromDatabase(getContext(), userBooksAdapter, pref.getString("sortBy", "name"), waitDialog, recyclerView, endlessScrollEventListener);

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


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        handleIntent(intent);
//        setIntent(intent);
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

                final String whereClause = "name LIKE '" + query + "%' OR writer LIKE '" + query + "%' AND quantity > 0";

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CONSTANTS.backendlessBookQuery(getContext(), waitDialog, whereClause, userBooksAdapter);
                    }
                });

                thread.start();
                recyclerView.requestFocus();
            }
        }
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
                    Toast.makeText(getContext(), "Book already selected", Toast.LENGTH_LONG).show();
                    goodToGo = false;
                }
            }
            if (goodToGo) {
                Log.i(TAG, "onBookClick: About to return intent");

                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.newlySelectedBook), CONSTANTS.bookListCached.get(position));
                getActivity().setResult(Activity.RESULT_OK, returnIntent);
                getActivity().finish();
            }
        } else if (CONSTANTS.canOrder && CONSTANTS.CURRENT_NUMBER_OF_ORDERS < CONSTANTS.maximum_order_per_day) {
            Intent intent = new Intent(getContext(), PlaceOrderActivity.class);
            intent.putExtra("selectedBook", CONSTANTS.bookListCached.get(position));
//            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdBooklist());
//            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
            Log.i(TAG, "onBookClick: Book: " + CONSTANTS.bookListCached.get(position));
            startActivityForResult(intent, PLACE_ORDER_RETURN_REQUEST_CODE);
        }

        else {
            // Meaning can't order
            if(!CONSTANTS.canOrder)
                CONSTANTS.showErrorDialog(getContext(), "Can't Place Order", CONSTANTS.cannot_order_message, "Okay", null, 0);
            else
                CONSTANTS.showErrorDialog(getContext(), "Can't Place Order", "Daily order limit exceeded. Please try again tomorrow", "Okay", null, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_ORDER_RETURN_REQUEST_CODE){
            Log.i(TAG, "onActivityResult: came here");
            if(!CONSTANTS.isShowingDefaultBooklist()) {
                CONSTANTS.bookListCached.clear();
                CONSTANTS.bookListCached.addAll(CONSTANTS.tempBookListCached);
                CONSTANTS.setShowingDefaultBooklist(true);
                userBooksAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showSortByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort By")

                .setItems(R.array.sortByArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String sortBy;
                        if (which == 0)
                            sortBy = "name";
                        else
                            sortBy = "writer";
                        Log.i("booklist_retrieve", "booklist: sortBy = " + sortBy);

                        final Dialog waitDialog = new Dialog(getContext());
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_please_wait);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CONSTANTS.freshRetrieveFromDatabase(getContext(), userBooksAdapter, sortBy, waitDialog, recyclerView, endlessScrollEventListener);

                            }
                        });

                        thread.start();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        if(view == sortImageButton){
            showSortByDialog();
        }
    }
}