package com.example.gronthomongol.ui.main.user.archive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.main.user.activity.UserOrderDetailsActivity;
import com.example.gronthomongol.ui.util.adapters.OrdersAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;
import com.example.gronthomongol.ui.main.admin.activity.AdminOrderDetailsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewOrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderClickListener {
    private OrdersAdapter orderlistAdapterRV;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvNoOrder;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;

    private int fromActivityID;
    private int ID_ORDER_DETAILS_ADMIN = 91;
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if((boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu_orders, menu);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menuMain_orders_filter){
            showFilterByDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewOrdersActivity.this);
        builder.setTitle("Filter By").setItems(R.array.filterByArray, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        if (which != CONSTANTS.currentOrderFilter) {
                            String whereClause = CONSTANTS.NULLMARKER;
                            if (which == 0)
                                whereClause = CONSTANTS.NULLMARKER;     // sensitive
                            else if (which == 1)
                                whereClause = "delivered = FALSE";
                            else if (which == 2)
                                whereClause = "delivered = FALSE AND paid = TRUE";
                            else if (which == 3)
                                whereClause = "delivered = FALSE AND paid = FALSE";
                            else if (which == 4)
                                whereClause = "delivered = TRUE";
                            Log.i("orderlist_retrieve", "orderlist: filterBy = " + whereClause);

                            final Dialog waitDialog = new Dialog(ViewOrdersActivity.this);
                            waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            waitDialog.setCancelable(false);
                            waitDialog.setContentView(R.layout.dialog_please_wait);
                            waitDialog.show();

                            final String finalWhereClause = whereClause;
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    CONSTANTS.freshOrderRetrieveFromDatabase(ViewOrdersActivity.this, orderlistAdapterRV, finalWhereClause, waitDialog, endlessScrollEventListener, which);
                                }
                            });

                            thread.start();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        orderlistAdapterRV = new OrdersAdapter(CONSTANTS.myOrdersCached, getApplicationContext(), this);
        recyclerView.setAdapter(orderlistAdapterRV);

        // Store this adapter in CONSTANT so that it can be used to update order list on real time from booklist
        CONSTANTS.orderlistAdapterRV = orderlistAdapterRV;

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, RecyclerView recyclerView) {
                Log.i(TAG, "onLoadMore: ");
                
                if(CONSTANTS.myOrdersCached.size() > CONSTANTS.getMyOrderPageSize() - 1) {

                    Log.i(TAG, "onLoadMore: Requesting more orders");

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

        final Dialog dialog = new Dialog(ViewOrdersActivity.this);
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
                        Log.i(TAG, "handleResponse: Loading orderedBooks successful. List size: " + response.size());
                        ArrayList<Book> orderedBooks = new ArrayList<>(response);
                        dialog.dismiss();
                        if((boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
                            Intent intent = new Intent(ViewOrdersActivity.this, AdminOrderDetailsActivity.class);
                            intent.putExtra("orderedBooks", orderedBooks);
                            intent.putExtra("currentOrder", (Serializable) CONSTANTS.myOrdersCached.get(position));
                            startActivityForResult(intent, ID_ORDER_DETAILS_ADMIN);
                        }
                        else {
                            Intent intent = new Intent(ViewOrdersActivity.this, UserOrderDetailsActivity.class);
                            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdViewOrders());
                            intent.putExtra("orderedBooks", orderedBooks);
                            intent.putExtra("currentOrder", (Serializable) CONSTANTS.myOrdersCached.get(position));
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        dialog.dismiss();
                        String title;
                        String message;
                        if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            CONSTANTS.showErrorDialog(ViewOrdersActivity.this, title, message, "Okay", null, 0);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ID_ORDER_DETAILS_ADMIN){
            if(resultCode == RESULT_OK){
                orderlistAdapterRV.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}