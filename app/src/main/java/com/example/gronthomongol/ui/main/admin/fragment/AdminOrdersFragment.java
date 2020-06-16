package com.example.gronthomongol.ui.main.admin.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.main.admin.activity.AdminOrderDetailsActivity;
import com.example.gronthomongol.ui.main.user.activity.UserOrderDetailsActivity;
import com.example.gronthomongol.ui.main.user.archive.ViewOrdersActivity;
import com.example.gronthomongol.ui.util.adapters.OrdersAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdminOrdersFragment extends Fragment implements OrdersAdapter.OnOrderClickListener{

    private OrdersAdapter ordersAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView noOrderTextView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;

    private int fromActivityID;
    private int ID_ORDER_DETAILS_ADMIN = 91;
    private final String TAG = "viewOrders";

    public AdminOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        findXmlElements(view);
        loadOrders();

        return view;
    }

    private void findXmlElements(View view){
        recyclerView = view.findViewById(R.id.recyclerViewAdminOrder);
        progressBar = view.findViewById(R.id.progressBarAdminOrder);
        noOrderTextView = view.findViewById(R.id.noOrderTextViewAdminOrder);
    }

    private void loadOrders(){
        if(CONSTANTS.myOrdersCached.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            noOrderTextView.setVisibility(View.VISIBLE);
        }
        else {
            setUpRecyclerView();
        }
    }

    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 0));
        ordersAdapter = new OrdersAdapter(CONSTANTS.myOrdersCached, getContext(), this);
        recyclerView.setAdapter(ordersAdapter);

        // Store this adapter in CONSTANT so that it can be used to update order list on real time from booklist
        CONSTANTS.orderlistAdapterRV = ordersAdapter;

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) recyclerViewLayoutManager) {
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
                            ordersAdapter.notifyDataSetChanged();
                            Log.i(TAG, "handleResponse: Order list refreshed");
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i(TAG, "handleFault: " + fault.getMessage());
                            if(fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless))){
                                Toast.makeText(getContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Error retrieving data from the database", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        };

        recyclerView.addOnScrollListener(endlessScrollEventListener);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if((boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
//            MenuInflater inflater = getActivity().getMenuInflater();
//            inflater.inflate(R.menu.options_menu_orders, menu);
//            return true;
//        }
//
//        return false;
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if(id == R.id.menuMain_orders_filter){
//            showFilterByDialog();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void showFilterByDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

                    final Dialog waitDialog = new Dialog(getContext());
                    waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    waitDialog.setCancelable(false);
                    waitDialog.setContentView(R.layout.dialog_please_wait);
                    waitDialog.show();

                    final String finalWhereClause = whereClause;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CONSTANTS.freshOrderRetrieveFromDatabase(getContext(), ordersAdapter, finalWhereClause, waitDialog, endlessScrollEventListener, which);
                        }
                    });

                    thread.start();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onOrderClick(final int position) {
        //Toast.makeText(getApplicationContext(), "You have clicked an order", Toast.LENGTH_SHORT).show();
        final Dialog dialog = new Dialog(getContext());
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
                        Intent intent = new Intent(getContext(), AdminOrderDetailsActivity.class);
                        intent.putExtra("orderedBooks", orderedBooks);
                        intent.putExtra("currentOrder", (Serializable) CONSTANTS.myOrdersCached.get(position));
                        startActivityForResult(intent, ID_ORDER_DETAILS_ADMIN);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        dialog.dismiss();
                        String title;
                        String message;
                        if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            CONSTANTS.showErrorDialog(getContext(), title, message, "Okay", null, 0);
                        }
                        else {
                            Toast.makeText(getContext(), "Error in communication. Please try again later", Toast.LENGTH_LONG).show();
                        }
                        Log.i(TAG, "handleFault: loading ordered books failed" + fault.getMessage());
                    }
                });
            }
        });

        thread.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ID_ORDER_DETAILS_ADMIN){
            if(resultCode == getActivity().RESULT_OK){
                ordersAdapter.notifyDataSetChanged();
            }
        }
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}