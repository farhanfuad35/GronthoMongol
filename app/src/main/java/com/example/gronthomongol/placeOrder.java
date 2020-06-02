package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

public class placeOrder extends AppCompatActivity {

    NumberPicker numberPicker;
    int numberOfBooks = 1;
    private EditText recipientName;
    private EditText phoneNumber;
    private EditText bookName;
    private EditText writerName;
    private EditText pricePerUnit;
    private EditText totalPrice;
    private EditText comment;
    private Button location;
    private Button placeOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        initializeGUIElements();
        numberPicker.setMinValue(CONSTANTS.getMIN_NO_OF_ORDERS_PER_USER_PER_BOOK());
        numberPicker.setMaxValue(CONSTANTS.getMAX_NO_OF_ORDERS_PER_USER_PER_BOOK());

        numberPicker.setOnValueChangedListener(onValueChangeListener);
    }


    NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            numberOfBooks = newVal;
        }
    };

    private void initializeGUIElements(){
        numberPicker = findViewById(R.id.numPOrder_NumPicker);
        writerName = findViewById(R.id.etPlaceOrder_WriterName);
        pricePerUnit = findViewById(R.id.etPlaceOrder_PricePerUnit);
        totalPrice = findViewById(R.id.etPlaceOrder_TotalPrice);
        comment = findViewById(R.id.etPlaceOrder_Comment);
        location = findViewById(R.id.btnPlaceOrder_location);
        placeOrder = findViewById(R.id.btnPlaceOrder_PlaceOrder);
        recipientName = findViewById(R.id.etPlaceOrder_RecipientName);
        phoneNumber = findViewById(R.id.etPlaceOrder_PhoneNumber);
        bookName = findViewById(R.id.etPlaceOrder_BookName);
    }
}