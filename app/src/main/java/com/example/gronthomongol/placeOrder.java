package com.example.gronthomongol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class placeOrder extends AppCompatActivity {

//    private NumberPicker numberPicker;
//    private EditText etBook_Name;
//    private EditText etWriter_Name;
//    private EditText etpricePerUnit;
//    private EditText etbkashTxnId;
    private EditText etRecipient_Name;
    private EditText etContact_No;
    private EditText etAddress;
    private EditText etTotal_Price;
    private EditText etComment;
    private Button btnAddress;
    private Button btnPlaceOrder;
    private ArrayList<Book> orderedBooks = new ArrayList<Book>(5);
    private int followedByProcessID;
    private Button btnAddMore;

    //    private String Book_Name;
    //    private String Writer_Name;
    //    private int pricePerUnit;
    //    private String bkashTxnId;

    private String Recipient_Name;
    private String Contact_No;

    private int Total_Price = 0;    // Updated As soon as the activity starts. Check SetValues()
    private String Comment;
    private String Delivery_Address;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private PlaceOrderAdapterRV placeOrderAdapterRV;

    private final String TAG = "placeOrder";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        setTitle("Order Book");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_PlaceOrder);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeGUIElements();
        initializeRecyclerView();

        initializeItemTouchHelper();


//        numberPicker.setMinValue(CONSTANTS.getMIN_NO_OF_ORDERS_PER_USER_PER_BOOK());
//        numberPicker.setMaxValue(CONSTANTS.getMAX_NO_OF_ORDERS_PER_USER_PER_BOOK());
//        numberPicker.setOnValueChangedListener(onValueChangeListener);

        orderedBooks.add((Book) getIntent().getSerializableExtra("selectedBook"));
        Log.i("placeorder", "ordered book array size = " + orderedBooks.size());

        followedByProcessID = getIntent().getIntExtra("ID",0);


        setFields();

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(etRecipient_Name) && !isEmpty(etContact_No) && !isEmpty(etAddress)){
                    // First Check if orderedBookList is empty or not
                    if(orderedBooks.size() == 0){
                        Toast.makeText(getApplicationContext(), getString(R.string.add_atleast_one_book), Toast.LENGTH_LONG).show();
                    }
                    else {

                        final Dialog dialog = new Dialog(placeOrder.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.loadingdialog);
                        dialog.show();

                        getValues();
                        if (Comment.isEmpty())
                            Comment = CONSTANTS.NULLMARKER;
                        final Order newOrder = new Order(Backendless.UserService.CurrentUser(), Recipient_Name, orderedBooks, Contact_No, Comment,
                                Delivery_Address, Total_Price);

                        Log.i(TAG, "onClick: newOrder Created");
                        Log.i(TAG, "onClick: orderId = " + newOrder.getOrderId());

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                newOrder.saveOrderInBackendless(orderedBooks, placeOrder.this, dialog);
                            }
                        });

                        thread.start();

                    }
                }
            }
        });

        btnAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), booklist.class);
                intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdPlaceOrderAddMoreBook());
                intent.putExtra(getString(R.string.orderedBookList), orderedBooks);
                startActivityForResult(intent, CONSTANTS.getIdPlaceOrderAddMoreBook());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // User Clicked on Add More Book and have come back after Selecting a new Book
        if(requestCode == CONSTANTS.getIdPlaceOrderAddMoreBook()){
            if(resultCode == Activity.RESULT_OK){

                Book newBook = (Book) data.getSerializableExtra(getString(R.string.newlySelectedBook));
                orderedBooks.add(newBook);
                placeOrderAdapterRV.notifyDataSetChanged();
                // Update Total Price
                Total_Price = Total_Price + newBook.getPrice();
                etTotal_Price.setText(Integer.toString(Total_Price));
            }
        }

    }

    private void initializeItemTouchHelper(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(placeOrder.this, "on Move", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Toast.makeText(placeOrder.this, "on Swiped ", Toast.LENGTH_SHORT).show();
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                // Update Total_Price
                Total_Price = Total_Price - orderedBooks.get(position).getPrice();
                etTotal_Price.setText(Integer.toString(Total_Price));
                orderedBooks.remove(position);
                placeOrderAdapterRV.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Book Removed", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void initializeRecyclerView() {
        rvLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        placeOrderAdapterRV = new PlaceOrderAdapterRV(orderedBooks, getApplicationContext());
        recyclerView.setAdapter(placeOrderAdapterRV);
    }


//    NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
//        @Override
//        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//            numberOfBooks = newVal;
//        }
//    };

    private void initializeGUIElements(){
//        numberPicker = findViewById(R.id.numPOrder_NumPicker);
//        etWriter_Name = findViewById(R.id.etPlaceOrder_WriterName);
//        etpricePerUnit = findViewById(R.id.etPlaceOrder_PricePerUnit);
//        etBook_Name = findViewById(R.id.etPlaceOrder_BookName);
//        etbkashTxnId = findViewById(R.id.etPlaceOrder_BkashTxnId);
        etTotal_Price = findViewById(R.id.etPlaceOrder_TotalPrice);
        etComment = findViewById(R.id.etPlaceOrder_Comment);
        btnAddress = findViewById(R.id.btnPlaceOrder_location);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder_PlaceOrder);
        etRecipient_Name = findViewById(R.id.etPlaceOrder_RecipientName);
        etContact_No = findViewById(R.id.etPlaceOrder_PhoneNumber);
        etAddress = findViewById(R.id.etPlaceOrder_Address);
        recyclerView = findViewById(R.id.rvPlaceOrder_BookList);
        btnAddMore = findViewById(R.id.btnPlaceOrder_AddMore);
    }
    private void setFields(){
        etRecipient_Name.setText((CharSequence) Backendless.UserService.CurrentUser().getProperty("name"));
        etContact_No.setText((CharSequence) Backendless.UserService.CurrentUser().getProperty("contact_no"));
//        etBook_Name.setText(orderedBooks.get(0).getName());
//        etWriter_Name.setText(orderedBooks.get(0).getWriter());
//        etpricePerUnit.setText(Integer.toString(orderedBooks.get(0).getPrice()));
        Total_Price = orderedBooks.get(0).getPrice();   // Total Price Updated
        etTotal_Price.setText(Integer.toString(Total_Price));
    }
    private boolean isEmpty(EditText editText){
        if(editText.getText().toString().isEmpty()){
            editText.setError("Please enter a subject");
            return true;
        }
        return false;
    }

    private void getValues(){
        Recipient_Name = etRecipient_Name.getText().toString().trim();
        Contact_No = etContact_No.getText().toString().trim();
        Total_Price = Integer.parseInt(etTotal_Price.getText().toString());
        Comment = etComment.getText().toString().trim();
        Delivery_Address = etAddress.getText().toString();

//        Book_Name = etBook_Name.getText().toString().trim();
//        Writer_Name = etWriter_Name.getText().toString().trim();
//        pricePerUnit = Integer.parseInt(etpricePerUnit.getText().toString());
//        bkashTxnId = etbkashTxnId.getText().toString().trim();
//        Book_Name = orderedBooks.get(0).getName();
//        Writer_Name = orderedBooks.get(0).getWriter();
//        pricePerUnit = orderedBooks.get(0).getPrice();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}