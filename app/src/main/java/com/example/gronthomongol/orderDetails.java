package com.example.gronthomongol;

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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.backendless.Backendless;

import java.util.ArrayList;
import java.util.List;

public class orderDetails extends AppCompatActivity {

    private TextView tvOrderId;
    private EditText etRecipient_Name;
    private EditText etContact_No;
    private EditText etAddress;
    private EditText etTotal_Price;
    private EditText etComment;
    private EditText etbkashTxnId;
    private Button btnAddress;
    private Button btnSubmitBkash;
    private ArrayList<Book> orderedBooks = new ArrayList<Book>(CONSTANTS.getMaxNoOfBooksPerUserPerOrder());
    private int followedByProcessID;
    private Button btnAddMore;
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

    private final String TAG = "orderDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        setTitle("Order Details");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_PlaceOrder);
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

        btnSubmitBkash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txnId = etbkashTxnId.getText().toString().trim();
                if(txnId.isEmpty()){
                    etbkashTxnId.setError("Please enter the txnId");
                }
                else{
                    showTxnIdConfirmationDialog();
                }
            }
        });

    }

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
            etbkashTxnId.setFocusable(false);
            etbkashTxnId.setClickable(false);
            etbkashTxnId.setFocusableInTouchMode(false);
            etbkashTxnId.setCursorVisible(false);
            btnSubmitBkash.setVisibility(View.GONE);
        }
        tvOrderId.setText("Order ID # " + currentOrder.getOrderId());
    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        placeOrderAdapterRV = new PlaceOrderAdapterRV(orderedBooks, getApplicationContext());
        recyclerView.setAdapter(placeOrderAdapterRV);
    }

    private void initializeGUIElements() {
        etTotal_Price = findViewById(R.id.etOrderDetails_TotalPrice);
        etComment = findViewById(R.id.etOrderDetails_Comment);
        btnSubmitBkash = findViewById(R.id.btnOrderDetails_SubmitBkash);
        etRecipient_Name = findViewById(R.id.etOrderDetails_RecipientName);
        etContact_No = findViewById(R.id.etOrderDetails_PhoneNumber);
        etAddress = findViewById(R.id.etOrderDetails_Address);
        recyclerView = findViewById(R.id.rvOrderDetails_BookList);
        etbkashTxnId = findViewById(R.id.etOrderDetails_BkashTxnId);
        tvOrderId = findViewById(R.id.tvOrderDetails_OrderId);
    }

    private void showTxnIdConfirmationDialog()
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(orderDetails.this);
        alertDialogBuilder.setTitle("Confirm TxnId");
        alertDialogBuilder.setMessage("Is the TxnId: '" + txnId + "' correct?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        currentOrder.setbKashTxnId(txnId);
                        currentOrder.setPaid(true);
                        final Dialog dialog = new Dialog(orderDetails.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.dialog_please_wait);
                        dialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Save newly updated object
                                currentOrder.updateOrderOnDatabase(orderDetails.this, dialog, "bkash");
                            }
                        });

                        thread.start();
                    }
                });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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