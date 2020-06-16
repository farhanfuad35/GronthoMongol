package com.example.gronthomongol.ui.main.admin.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

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
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Request;
import com.example.gronthomongol.ui.main.admin.archive.RequestListActivity;
import com.example.gronthomongol.ui.util.adapters.RequestsAdapter;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;

import java.util.List;

public class RequestsFragment extends Fragment implements RequestsAdapter.OnRequestClickListener{

    private RequestsAdapter requestsAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private EndlessScrollEventListener endlessScrollEventListener;

    private String TAG = "requestlist";

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        findXmlElements(view);
        loadRequests();

        return view;
    }

    private void findXmlElements(View view){
        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        progressBar = view.findViewById(R.id.progressBarRequests);
    }

    private void loadRequests(){
        if(CONSTANTS.userRequestsCached == null) {
            // Request list wasn't retrieved at the beginning
            final Dialog waitDialog = new Dialog(getContext());
            waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            waitDialog.setCancelable(false);
            waitDialog.setContentView(R.layout.dialog_please_wait);
            waitDialog.show();
            retrieveRequestData(waitDialog);
        }
        else {
            setUpRecyclerView();
        }
    }

    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 0));
        requestsAdapter = new RequestsAdapter(CONSTANTS.userRequestsCached, getContext(), this);
        recyclerView.setAdapter(requestsAdapter);

        endlessScrollEventListener = new EndlessScrollEventListener((LinearLayoutManager) recyclerViewLayoutManager) {
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
                            requestsAdapter.notifyDataSetChanged();
                            Log.i(TAG, "handleResponse: Request list refreshed");
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
                setUpRecyclerView();
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
                else{
                    CONSTANTS.showErrorDialog(getContext(), "Error Occurred", "Sorry cannot retrieve the request list right now",
                            "Okay", null, 19);
                }
            }
        });

    }

    @Override
    public void onRequestClick(final int position) {
        String title = "কাঙ্ক্ষিত বইটি কি এ্যাড করা হয়েছে?";
        //String message = "This operation is going to delete the order from the database";
        String positiveButton = "হ্যা";
        String negativeButton = "না";

        Context context = getContext();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity) context));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                final Dialog waitDialog = new Dialog(getContext());
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_notifying_user);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        CONSTANTS.userRequestsCached.get(position).markAsResolvedAndNotify(getContext(), waitDialog, requestsAdapter);
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
                final Dialog waitDialog = new Dialog(getContext());
                waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                waitDialog.setCancelable(false);
                waitDialog.setContentView(R.layout.dialog_removing_request);
                waitDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO
                        CONSTANTS.userRequestsCached.get(position).removeRequest(getContext(), waitDialog, requestsAdapter);
                    }
                });

                thread.start();
                Log.i("request_update", "onClick: Sent the request for update");
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}