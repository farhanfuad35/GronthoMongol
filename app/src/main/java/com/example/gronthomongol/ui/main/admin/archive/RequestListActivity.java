package com.example.gronthomongol.ui.main.admin.archive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Request;
import com.example.gronthomongol.ui.util.adapters.RequestlistAdapterRV;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;

import java.util.List;

public class RequestListActivity extends AppCompatActivity implements RequestlistAdapterRV.OnRequestClickListener {
    private RequestlistAdapterRV requestlistAdapterRV;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;

    private String TAG = "requestlist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        setTitle("Book Requests");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_RequestList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressBar = findViewById(R.id.pbRequestlist_progressBar);
        recyclerView = findViewById(R.id.rvRequestList_RequestList);

        if(CONSTANTS.userRequestsCached == null) {
            // Request list wasn't retrieved at the beginning
            final Dialog waitDialog = new Dialog(RequestListActivity.this);
            waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            waitDialog.setCancelable(false);
            waitDialog.setContentView(R.layout.dialog_please_wait);
            waitDialog.show();

            retrieveRequestData(waitDialog);
        }
        else {
            initializeRecyclerView();
        }
    }

    private void retrieveRequestData(final Dialog dialog) {
        // REQUIREMENTS: USER IS ASSUMED AN ADMIN BY DEFAULT
        final DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.addAllProperties();
        dataQueryBuilder.setWhereClause("resolved = FALSE");
        dataQueryBuilder.setSortBy("created");
        dataQueryBuilder.setPageSize(CONSTANTS.getRequestsPageSize()).setOffset(CONSTANTS.getRequestsOffset());
        Backendless.Data.of(Request.class).find(dataQueryBuilder, new AsyncCallback<List<Request>>() {
            @Override
            public void handleResponse(List<Request> response) {
                CONSTANTS.userRequestsCached = response;
                CONSTANTS.setRequestsOffset(CONSTANTS.getRequestsOffset() + CONSTANTS.getRequestsPageSize());
                CONSTANTS.setUserRequestsQueryBuilder(dataQueryBuilder);
                dialog.dismiss();
                initializeRecyclerView();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                dialog.dismiss();
                String title;
                String message;
                if( fault.getMessage().equals(getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog(RequestListActivity.this, title, message, "Okay", null, 0);
                }
                else{
                    CONSTANTS.showErrorDialog(RequestListActivity.this, "Error Occurred", "Sorry cannot retrieve the request list right now",
                            "Okay", null, 19);
                }
            }
        });

    }


    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        requestlistAdapterRV = new RequestlistAdapterRV(CONSTANTS.userRequestsCached, getApplicationContext(), this);
        recyclerView.setAdapter(requestlistAdapterRV);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) rvLayoutManager) {
            @Override
            public void onLoadMore(int pageNum, RecyclerView recyclerView) {

                if(CONSTANTS.userRequestsCached.size() > CONSTANTS.getPageSize() - 1) {

                    progressBar.setVisibility(View.VISIBLE);

                    DataQueryBuilder queryBuilder = CONSTANTS.getUserRequestsQueryBuilder();
                    queryBuilder.prepareNextPage();

                    Log.i(TAG, "onLoadMore: Came to OnloadMore");

                    Backendless.Data.of(Request.class).find(queryBuilder, new AsyncCallback<List<Request>>() {
                        @Override
                        public void handleResponse(List<Request> response) {
                            CONSTANTS.userRequestsCached.addAll(response);
                            progressBar.setVisibility(View.GONE);
                            requestlistAdapterRV.notifyDataSetChanged();
                            Log.i(TAG, "handleResponse: Request list refreshed");
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
    public void onRequestClick(final int position) {
        String title = "Select an operation";
        //String message = "This operation is going to delete the order from the database";
        String positiveButton = "Book Added";
        String negativeButton = "Cannot Add";
        Context context = RequestListActivity.this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity) context));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                final Dialog waitDialog = new Dialog(RequestListActivity.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_notifying_user);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        CONSTANTS.userRequestsCached.get(position).markAsResolvedAndNotify(RequestListActivity.this, waitDialog, requestlistAdapterRV);
                    }
                });

                thread.start();

                Log.i("request_update", "onClick: Sent the request for update");
            }
        });
        alertDialogBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final Dialog waitDialog = new Dialog(RequestListActivity.this);
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_removing_request);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        CONSTANTS.userRequestsCached.get(position).removeRequest(RequestListActivity.this, waitDialog, requestlistAdapterRV);
                    }
                });

                thread.start();

                Log.i("request_update", "onClick: Sent the request for update");
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}