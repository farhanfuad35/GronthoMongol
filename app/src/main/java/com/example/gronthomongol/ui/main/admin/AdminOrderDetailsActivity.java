package com.example.gronthomongol.ui.main.admin;

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

import com.example.gronthomongol.ui.util.adapters.CartAdapter;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Order;

import java.util.ArrayList;

public class AdminOrderDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView orderIdTextView;
    private TextView orderStatusTextView;
    private TextView bkashNumberTextView;

    private TextView nameTextView;
    private TextView phoneNumberTextView;
    private TextView addressTextView;
    private TextView totalPriceTextView;
    private TextView commentTextView;
    private TextView transactionIdTextView;

    private Button rejectButton;
    private Button deliveredButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private CartAdapter cartAdapter;

    private ArrayList<Book> orderedBooks = new ArrayList<Book>(CONSTANTS.getMaxNoOfBooksPerUserPerOrder());
    private int followedByProcessID;
    private String Recipient_Name;
    private String Contact_No;
    private int Total_Price = 0;    // Updated As soon as the activity starts. Check SetValues()
    private String Comment;
    private String Delivery_Address;
    private String txnId;
    private Order currentOrder;

    private final String TAG = "orderDetails_admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_admin);

        Log.i(TAG, "onCreate: in OrderDetails");

        orderedBooks = (ArrayList<Book>) getIntent().getSerializableExtra("orderedBooks");
        currentOrder = (Order) getIntent().getSerializableExtra("currentOrder");

        Log.i(TAG, "onCreate: orderedBooks Size" + orderedBooks.size());
        Log.i(TAG, "onCreate: CurrentOrder ID: " + currentOrder.getOrderId());

        findXmlElements();
        setUpRecyclerView();
        setFields();
        setUpListeners();
    }

    private void findXmlElements() {
        orderIdTextView = findViewById(R.id.orderIdTextViewOrderDetailsAdmin);
        orderStatusTextView = findViewById(R.id.orderStatusViewOrderDetailsAdmin);
        bkashNumberTextView = findViewById(R.id.bkashNumberTextViewOrderDetailsAdmin);

        nameTextView = findViewById(R.id.nameTextViewOrderDetailsAdmin);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextViewOrderDetailsAdmin);
        addressTextView = findViewById(R.id.addressTextViewOrderDetailsAdmin);
        totalPriceTextView = findViewById(R.id.totalPriceTextViewOrderDetailsAdmin);
        commentTextView = findViewById(R.id.commentTextViewOrderDetailsAdmin);
        transactionIdTextView = findViewById(R.id.transactionIdTextViewOrderDetailsAdmin);

        rejectButton = findViewById(R.id.rejectButtonOrderDetailsAdmin);
        deliveredButton = findViewById(R.id.deliveredButtonOrderDetailsAdmin);

        recyclerView = findViewById(R.id.recyclerViewOrderDetailsAdmin);
    }

    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        cartAdapter = new CartAdapter(orderedBooks, getApplicationContext());
        recyclerView.setAdapter(cartAdapter);
    }

    @SuppressLint("ResourceAsColor")
    private void setFields() {
        orderIdTextView.setText("অর্ডার আইডি # " + currentOrder.getOrderId());
        nameTextView.setText(currentOrder.getRecipient_Name());
        phoneNumberTextView.setText(currentOrder.getContact_No());
        addressTextView.setText(currentOrder.getAddress());
        Total_Price = currentOrder.getTotal_Price();
        totalPriceTextView.setText(Integer.toString(Total_Price));

        if(currentOrder.getComment().equals(CONSTANTS.NULLMARKER)){
            commentTextView.setVisibility(View.GONE);
        }
        else{
            commentTextView.setText(currentOrder.getComment());
            commentTextView.setHint(getString(R.string.comment_neat));
        }
        if(!currentOrder.getbKashTxnId().equals(CONSTANTS.NULLMARKER)){
            transactionIdTextView.setText(currentOrder.getbKashTxnId());
        }
        else {
            transactionIdTextView.setVisibility(View.GONE);
        }

        if(currentOrder.isDelivered()){
            orderStatusTextView.setText("ডেলিভার করা হয়েছে");
            orderStatusTextView.setTextColor(Color.parseColor("#157015"));
            rejectButton.setVisibility(View.GONE);
            deliveredButton.setVisibility(View.GONE);
        }
        else if(currentOrder.isPaid()){
            orderStatusTextView.setText("পেমেন্ট করা হয়েছে");
            orderStatusTextView.setTextColor(Color.parseColor("#918200"));
        }
        else{
            orderStatusTextView.setText("পেমেন্ট করা হয়নি");
            orderStatusTextView.setTextColor(Color.parseColor("#800A0A"));
        }
    }

    private void setUpListeners(){
        deliveredButton.setOnClickListener(this);
        rejectButton.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        if(view == deliveredButton){
            currentOrder.setDelivered(true);

            final Dialog dialog = new Dialog(AdminOrderDetailsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_please_wait);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    currentOrder.updateOrderOnDatabase(AdminOrderDetailsActivity.this, dialog, "delivered");
                }
            });

            thread.start();
        } else if(view == rejectButton){
            String title = "Are you sure?";
            String message = "This operation is going to delete the order from the database";
            String positiveButton = "Yes";
            String negativeButton = "Cancel";
            Context context = AdminOrderDetailsActivity.this;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity) context));
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    dialog.dismiss();
                    final Dialog waitDialog = new Dialog(AdminOrderDetailsActivity.this);
                    waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    waitDialog.setCancelable(false);
                    waitDialog.setContentView(R.layout.dialog_deleting_order);
                    waitDialog.show();

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            currentOrder.deleteOrderOnDatabase(AdminOrderDetailsActivity.this, waitDialog);
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
    }
}