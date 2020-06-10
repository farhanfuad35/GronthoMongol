package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class orderDetails_admin extends AppCompatActivity {

    private TextView tvOrderId;
    private EditText etRecipient_Name;
    private EditText etContact_No;
    private EditText etAddress;
    private EditText etTotal_Price;
    private EditText etComment;
    private EditText etbkashTxnId;
    private Button btnDelete;
    private Button btnDelivered;
    private TextView tvOrderStatus;
    private ArrayList<Book> orderedBooks = new ArrayList<Book>(CONSTANTS.getMaxNoOfBooksPerUserPerOrder());
    private int followedByProcessID;
    private String Recipient_Name;
    private String Contact_No;
    private int Total_Price = 0;    // Updated As soon as the activity starts. Check SetValues()
    private String Comment;
    private String Delivery_Address;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private PlaceOrderAdapterRV placeOrderAdapterRV;
    private String txnId;
    private Order currentOrder;

    private final String TAG = "orderDetails_admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_admin);

        setTitle("Order Details");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_orderDetails_admin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Log.i(TAG, "onCreate: in OrderDetails");

        orderedBooks = (ArrayList<Book>) getIntent().getSerializableExtra("orderedBooks");
        currentOrder = (Order) getIntent().getSerializableExtra("currentOrder");

        Log.i(TAG, "onCreate: orderedBooks Size" + orderedBooks.size());
        Log.i(TAG, "onCreate: CurrentOrder ID: " + currentOrder.getOrderId());

        initializeGUIElements();
        initializeRecyclerView();
        setFields();


        btnDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrder.setDelivered(true);

                final Dialog dialog = new Dialog(orderDetails_admin.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_please_wait);
                dialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentOrder.updateOrderOnDatabase(orderDetails_admin.this, dialog, "delivered");
                    }
                });

                thread.start();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "Are you sure?";
                String message = "This operation is going to delete the order from the database";
                String positiveButton = "Yes";
                String negativeButton = "Cancel";
                Context context = orderDetails_admin.this;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity) context));
                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Dialog waitDialog = new Dialog(orderDetails_admin.this);
                        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        waitDialog.setCancelable(false);
                        waitDialog.setContentView(R.layout.dialog_deleting_order);
                        waitDialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                currentOrder.deleteOrderOnDatabase(orderDetails_admin.this, waitDialog);
                            }
                        });

                        thread.start();

                        Log.i("order_deletion", "onClick: Sent the order for deletion");
                    }
                });
                alertDialogBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

    }

    @SuppressLint("ResourceAsColor")
    private void setFields() {
        etRecipient_Name.setText(currentOrder.getRecipient_Name());
        etContact_No.setText(currentOrder.getContact_No());
        Total_Price = currentOrder.getTotal_Price();
        etTotal_Price.setText(Integer.toString(Total_Price));
        etAddress.setText(currentOrder.getAddress());
        if(currentOrder.getComment().equals(CONSTANTS.NULLMARKER)){
            etComment.setVisibility(View.GONE);
        }
        else{
            etComment.setText(currentOrder.getComment());
            etComment.setHint(getString(R.string.comment_neat));
        }
        if(!currentOrder.getbKashTxnId().equals(CONSTANTS.NULLMARKER)){
            etbkashTxnId.setText(currentOrder.getbKashTxnId());
        }
        else {
            etbkashTxnId.setVisibility(View.GONE);
        }
        tvOrderId.setText("Order ID # " + currentOrder.getOrderId());

        if(currentOrder.isDelivered()){
            tvOrderStatus.setText("Delivered");
            tvOrderStatus.setTextColor(Color.parseColor("#157015"));
            btnDelivered.setVisibility(View.GONE);
        }
        else if(currentOrder.isPaid()){
            tvOrderStatus.setText("Payment Completed");
            tvOrderStatus.setTextColor(Color.parseColor("#918200"));
        }
        else{
            tvOrderStatus.setText("Payment Incomplete");
            tvOrderStatus.setTextColor(Color.parseColor("#800A0A"));
        }
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        placeOrderAdapterRV = new PlaceOrderAdapterRV(orderedBooks, getApplicationContext());
        recyclerView.setAdapter(placeOrderAdapterRV);
    }

    private void initializeGUIElements() {
        etTotal_Price = findViewById(R.id.etOrderDetails_admin_TotalPrice);
        etComment = findViewById(R.id.etOrderDetails_admin_Comment);
        etRecipient_Name = findViewById(R.id.etOrderDetails_admin_RecipientName);
        etContact_No = findViewById(R.id.etOrderDetails_admin_PhoneNumber);
        etAddress = findViewById(R.id.etOrderDetails_admin_Address);
        recyclerView = findViewById(R.id.rvOrderDetails_admin_BookList);
        etbkashTxnId = findViewById(R.id.etOrderDetails_admin_BkashTxnId);
        tvOrderId = findViewById(R.id.tvOrderDetails_admin_OrderId);
        btnDelete = findViewById(R.id.btnOrderDetails_admin_Delete);
        btnDelivered = findViewById(R.id.btnOrderDetails_admin_MarkAsDelivered);
        tvOrderStatus = findViewById(R.id.tvOrderDetails_admin_orderStatus);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}