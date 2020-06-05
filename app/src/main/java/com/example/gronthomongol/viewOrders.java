package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.data.EventHandler;

import java.util.List;

public class viewOrders extends AppCompatActivity implements OrderlistAdapterRV.OnOrderClickListener {
    private OrderlistAdapterRV orderlistAdapterRV;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
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
        initializeRecyclerView();
    }


    private void initializeGUIElements(){
        recyclerView = findViewById(R.id.rvOrderList_OrderList);
        progressBar = findViewById(R.id.pbOrderlist_progressBar);
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
                        Toast.makeText(getApplicationContext(), "Error retrieving data from the database", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        recyclerView.addOnScrollListener(endlessScrollEventListener);
    }

    @Override
    public void onOrderClick(int position) {
        Toast.makeText(getApplicationContext(), "You have clicked an order", Toast.LENGTH_SHORT).show();
    }
}