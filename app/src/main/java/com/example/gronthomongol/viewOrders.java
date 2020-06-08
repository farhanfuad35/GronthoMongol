package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;
import com.backendless.rt.data.EventHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class viewOrders extends AppCompatActivity implements OrderlistAdapterRV.OnOrderClickListener {
    private OrderlistAdapterRV orderlistAdapterRV;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvNoOrder;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;
    private EventHandler<Order> orderEventHandler = Backendless.Data.of(Order.class).rt();

    private int fromActivityID;
    private final String TAG = "viewOrders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);
        setTitle("Orders");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_viewOrders);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initializeGUIElements();
        if(CONSTANTS.myOrdersCached.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            tvNoOrder.setVisibility(View.VISIBLE);
        }
        else {
            initializeRecyclerView();
            initiateRealTimeDatabaseListeners();
        }
    }


    public void initiateRealTimeDatabaseListeners() {
        // Update Listener
        Log.i(TAG, "initiateRealTimeDatabaseListeners: update Listener initiated");
        String whereClause = "Recipient_Email = '" + CONSTANTS.getCurrentUser().getEmail() + "'";
        orderEventHandler.addUpdateListener( whereClause, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order updatedOrder) {
                Log.i(TAG, "handleResponse: update listener triggered");
                for(int i=0; i<CONSTANTS.myOrdersCached.size(); i++){
                    if(CONSTANTS.myOrdersCached.get(i).equals(updatedOrder)){
                        CONSTANTS.myOrdersCached.remove(i);
                        CONSTANTS.myOrdersCached.add(i, updatedOrder);
                        orderlistAdapterRV.notifyDataSetChanged();
                    }
                }
                Log.i(TAG, "an Order object has been updated. Object ID - " + updatedOrder.getObjectId());

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Server reported an error while Updating book listener " + fault.getDetail());
            }
        });

        // Delete Listener
//
//        orderEventHandler.addDeleteListener(new AsyncCallback<Order>() {
//            @Override
//            public void handleResponse(Book deletedBook) {
//                Log.i(TAG, "an Order object has been deleted. Object ID - " + deletedBook.getObjectId());
//                if (CONSTANTS.bookListCached.contains(deletedBook)) {
//                    CONSTANTS.bookListCached.remove(deletedBook);
//                    booklistAdapterRV.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void handleFault(BackendlessFault fault) {
//                Log.e(TAG, "Server reported an error " + fault.getDetail());
//            }
//        });

    }

    private void initializeGUIElements(){
        recyclerView = findViewById(R.id.rvOrderList_OrderList);
        progressBar = findViewById(R.id.pbOrderlist_progressBar);
        tvNoOrder = findViewById(R.id.tvViewOrders_NoOrder);
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        orderlistAdapterRV = new OrderlistAdapterRV(CONSTANTS.myOrdersCached, getApplicationContext(), this);
        recyclerView.setAdapter(orderlistAdapterRV);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, RecyclerView recyclerView) {

                if(CONSTANTS.myOrdersCached.size() > CONSTANTS.getPageSize() - 1) {

                    progressBar.setVisibility(View.VISIBLE);

                    DataQueryBuilder queryBuilder = CONSTANTS.getOrderListQueryBuilder();
                    queryBuilder.prepareNextPage();

                    Log.i(TAG, "onLoadMore: Came to OnloadMore");

                    Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
                        @Override
                        public void handleResponse(List<Order> response) {
                            CONSTANTS.myOrdersCached.addAll(response);
                            progressBar.setVisibility(View.GONE);
                            orderlistAdapterRV.notifyDataSetChanged();
                            Log.i(TAG, "handleResponse: Order list refreshed");
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i(TAG, "handleFault: " + fault.getMessage());
                            if(fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless))){
                                Toast.makeText(getApplicationContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error retrieving data from the database", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        };

        recyclerView.addOnScrollListener(endlessScrollEventListener);
    }

    @Override
    public void onOrderClick(final int position) {
        //Toast.makeText(getApplicationContext(), "You have clicked an order", Toast.LENGTH_SHORT).show();

        final Dialog dialog = new Dialog(viewOrders.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_please_wait);
        dialog.show();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LoadRelationsQueryBuilder<Book> loadRelationsQueryBuilder;
                loadRelationsQueryBuilder = LoadRelationsQueryBuilder.of( Book.class );
                loadRelationsQueryBuilder.setRelationName( "orderedBookList" );
                String parentObjectId = CONSTANTS.myOrdersCached.get(position).getObjectId();

                Backendless.Data.of("Order").loadRelations(parentObjectId, loadRelationsQueryBuilder, new AsyncCallback<List<Book>>() {
                    @Override
                    public void handleResponse(List<Book> response) {
                        // TODO: Do something with it
                        ArrayList<Book> orderedBooks = new ArrayList<>(response);
                        Log.i(TAG, "handleResponse: Loading orderedBooks successful. List size: " + orderedBooks.size());
                        Intent intent = new Intent(viewOrders.this, orderDetails.class);
                        intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdViewOrders());
                        intent.putExtra("orderedBooks", orderedBooks);
                        intent.putExtra("currentOrder", (Serializable) CONSTANTS.myOrdersCached.get(position));
                        dialog.dismiss();
                        startActivity(intent);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        dialog.dismiss();
                        String title;
                        String message;
                        if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            CONSTANTS.showErrorDialog(viewOrders.this, title, message, "Okay");
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error in communication. Please try again later", Toast.LENGTH_LONG).show();
                        }
                        Log.i(TAG, "handleFault: loading ordered books failed" + fault.getMessage());
                    }
                });
            }
        });

        thread.start();


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}