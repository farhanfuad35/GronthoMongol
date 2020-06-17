package com.example.gronthomongol.ui.main.user.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

public class UserOrderDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView orderIdTextView;
    private TextView orderStatusTextView;
    private TextView bkashNumberTextView;
    private TextView nameTextView;
    private TextView phoneNumberTextView;
    private TextView addressTextView;
    private TextView totalPriceTextView;
    private EditText commentEditText;
    private EditText transactionIdEditText;
    private Button submitButton;
    private Button cancelButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recycleViewLayoutManager;
    private CartAdapter cartAdapter;

    private ArrayList<Book> orderedBooks = new ArrayList<Book>(CONSTANTS.getMaxNoOfBooksPerUserPerOrder());
    private int Total_Price = 0;    // Updated As soon as the activity starts. Check SetValues()
    private String trxId;
    private Order currentOrder;

    private final String TAG = "orderDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_user);

        Log.i(TAG, "onCreate: in OrderDetails");

        orderedBooks = (ArrayList<Book>) getIntent().getSerializableExtra("orderedBooks");
        currentOrder = (Order) getIntent().getSerializableExtra("currentOrder");

        Log.i(TAG, "onCreate: orderedBooks Size" + orderedBooks.size());
        Log.i(TAG, "onCreate: CurrentOrder ID: " + currentOrder.getOrderId());

        findXmlElements();
        setUpListeners();
        setUpRecyclerView();
        setFields();
    }

    private void findXmlElements() {
        orderIdTextView = findViewById(R.id.orderIdTextViewOrderDetailsUser);
        orderStatusTextView = findViewById(R.id.orderStatusViewOrderDetailsUser);

        bkashNumberTextView = findViewById(R.id.bkashNumberTextViewOrderDetailsUser);

        recyclerView = findViewById(R.id.recyclerViewOrderDetailsUser);

        nameTextView = findViewById(R.id.nameTextViewOrderDetailsUser);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextViewOrderDetailsUser);
        addressTextView = findViewById(R.id.addressTextViewOrderDetailsUser);
        totalPriceTextView = findViewById(R.id.totalPriceTextViewOrderDetailsUser);
        commentEditText = findViewById(R.id.commentEditTextOrderDetailsUser);
        transactionIdEditText = findViewById(R.id.transactionIdEditTextOrderDetailsUser);

        submitButton = findViewById(R.id.submitButtonOrderDetailsUser);
        cancelButton = findViewById(R.id.cancelButtonOrderDetailsUser);
    }

    private void setUpListeners(){
        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        recycleViewLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recycleViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));
        cartAdapter = new CartAdapter(orderedBooks, getApplicationContext());
        recyclerView.setAdapter(cartAdapter);
    }

    private void setFields() {
        String orderId = "অর্ডার আইডি # " + currentOrder.getOrderId();
        String name = currentOrder.getRecipient_Name();
        String phoneNumber = currentOrder.getContact_No();
        String totalPrice = Integer.toString(currentOrder.getTotal_Price());
        String address = currentOrder.getAddress();

        orderIdTextView.setText(orderId);
        nameTextView.setText(name);
        phoneNumberTextView.setText(phoneNumber);
        totalPriceTextView.setText(totalPrice);
        addressTextView.setText(address);

        submitButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        if(!currentOrder.getComment().equals(CONSTANTS.NULLMARKER)){
            commentEditText.setText(currentOrder.getComment());
        }

        if(!currentOrder.getbKashTxnId().equals(CONSTANTS.NULLMARKER)){
            transactionIdEditText.setText(currentOrder.getbKashTxnId());
            transactionIdEditText.setFocusable(false);
            transactionIdEditText.setClickable(false);
            transactionIdEditText.setFocusableInTouchMode(false);
            transactionIdEditText.setCursorVisible(false);

            commentEditText.setFocusable(false);
            commentEditText.setClickable(false);
            commentEditText.setFocusableInTouchMode(false);
            commentEditText.setCursorVisible(false);

            submitButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }
    }

    private void showTrxIdConfirmationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserOrderDetailsActivity.this);
        alertDialogBuilder.setTitle("TrxID নিশ্চিত করুন");
        alertDialogBuilder.setMessage("এই TxnID: '" + trxId + "' কি সঠিক?");

        alertDialogBuilder.setPositiveButton("হ্যা",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        submitButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);

                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        currentOrder.setbKashTxnId(trxId);
                        currentOrder.setPaid(true);

                        final Dialog dialog = new Dialog(UserOrderDetailsActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.dialog_please_wait);
                        dialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Save newly updated object
                                currentOrder.updateOrderOnDatabase(UserOrderDetailsActivity.this, dialog, "bkash");
                            }
                        });

                        thread.start();
                    }
                });

        alertDialogBuilder.setNegativeButton("না", new DialogInterface.OnClickListener() {
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

    @Override
    public void onClick(View view) {
        if(view == submitButton){
            trxId = transactionIdEditText.getText().toString().trim();
            if(trxId.isEmpty()){
                transactionIdEditText.setError("সঠিক নয়");
            }
            else{
                showTrxIdConfirmationDialog();
            }
        } else if(view == cancelButton){

        }
    }
}