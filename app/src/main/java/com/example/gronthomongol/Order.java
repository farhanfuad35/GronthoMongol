package com.example.gronthomongol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Order implements Serializable {
    private String objectId;
    private Date created;
    private String orderId;
    private String Recipient_Name;
    private String Recipient_Email;
//    private String Book_Name;
//    private String Writer_Name;
//    private String Book_Object_ID;
//    private int Quantity;
    private String Contact_No;
    private String Address;
    private BackendlessUser user;
    private ArrayList<Book> orderedBookList;
    private String Comment;
    private int Total_Price;
    private String bKashTxnId;
    private boolean paid;
    private boolean delivered;     // 0 processing, 1 delivered


    public Order(BackendlessUser orderingUser, String recipient_Name, ArrayList<Book> orderedBook, String contact_No, String comment,
                 String address, int totalPrice) {
        Recipient_Name = recipient_Name;
//        Book_Name = orderedBook.getName();
//        Writer_Name = orderedBook.getWriter();
        Contact_No = contact_No;
        Comment = comment;
//        Quantity = quantity;
        Address = address;
        bKashTxnId = CONSTANTS.NULLMARKER;
        delivered=false;
        Recipient_Email = orderingUser.getEmail();
//        Book_Object_ID = orderedBook.getObjectId();
        this.user = orderingUser;
        orderedBookList = orderedBook;
        Total_Price = totalPrice;
        orderId = generateOrderId((String) orderingUser.getProperty("name"));
    }

    public Order(){

    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public ArrayList<Book> getOrderedBookList() {
        return orderedBookList;
    }

    public String generateOrderId(String orderingUserName){
        orderingUserName = orderingUserName.toLowerCase();
        Log.i("orderid", "generateOrderId: ordering User Name: " + orderingUserName);
        String orderId = orderingUserName.substring(0, 3);
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
        Date date = new Date();
        orderId = orderId + formatter.format(date);
        return orderId;
    }

    public void setOrderedBookList(ArrayList<Book> orderedBookList) {
        this.orderedBookList = orderedBookList;
    }

//    public String getBook_Object_ID() {
//        return Book_Object_ID;
//    }
//
//    public void setBook_Object_ID(String book_Object_ID) {
//        Book_Object_ID = book_Object_ID;
//    }
//
    public String getRecipient_Email() {
        return Recipient_Email;
    }

    public void setRecipient_Email(String recipient_Email) {
        Recipient_Email = recipient_Email;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public int getTotal_Price() {
        return Total_Price;
    }

    public void setTotal_Price(int total_Price) {
        Total_Price = total_Price;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public String getRecipient_Name() {
        return Recipient_Name;
    }

    public void setRecipient_Name(String recipient_Name) {
        Recipient_Name = recipient_Name;
    }

    @Override
    public boolean equals(Object order) {
        if (!(order instanceof Order)) {
            return false;
        }

        Order thatOrder = (Order) order;

        // Custom equality check here.
        return this.objectId.equals(thatOrder.objectId);
    }

//    public String getBook_Name() {
//        return Book_Name;
//    }
//
//    public void setBook_Name(String book_Name) {
//        Book_Name = book_Name;
//    }
//
//    public String getWriter_Name() {
//        return Writer_Name;
//    }
//
//    public void setWriter_Name(String writer_Name) {
//        Writer_Name = writer_Name;
//    }

    public String getContact_No() {
        return Contact_No;
    }

    public void setContact_No(String contact_No) {
        Contact_No = contact_No;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

//    public int getQuantity() {
//        return Quantity;
//    }
//
//    public void setQuantity(int quantity) {
//        Quantity = quantity;
//    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getbKashTxnId() {
        return bKashTxnId;
    }

    public void setbKashTxnId(String bKashTxnId) {
        this.bKashTxnId = bKashTxnId;
    }

    public void saveOrderInBackendless(final ArrayList<Book> orderedBooks, final Context context, final Dialog dialog){
        final ArrayList<BackendlessUser> orderingUser = new ArrayList<>(1);
        orderingUser.add(CONSTANTS.getCurrentUser());

        Backendless.Data.of(Order.class).save(this, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(final Order savedOrder) {
                Log.i("backendless_order", "handleResponse: Order has been saved successfully in database");


                // Relate the books with the Order
                Backendless.Data.of(Order.class).addRelation(savedOrder,
                        "orderedBookList:Book:n", orderedBooks, new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse(Integer response) {
                                Log.i("backendless_order", "handleResponse: The Relation Has Been Set");


                                // Relate the Order with the ordering User
                                Backendless.Data.of( Order.class ).setRelation(savedOrder, "user", orderingUser, new AsyncCallback<Integer>() {
                                    @Override
                                    public void handleResponse(Integer response) {
                                        Log.i("backendless_order", "handleResponse: User Relation Set Successfully. All done nicely");

                                        CONSTANTS.myOrdersCached.add(0,savedOrder);     // New Order always added at the beginning. STRICT last first sort must always be true

                                        // Update the remaining books number in the database
                                        for(int i=0; i<orderedBooks.size(); i++){
                                            int quantity = orderedBooks.get(i).getQuantity();
                                            orderedBooks.get(i).setQuantity(quantity - 1);          // TODO FIX THIS IF ORDERING QUANTITY CHANGES IN FUTURE
                                            Backendless.Data.of( Book.class ).save( orderedBooks.get(i), new AsyncCallback<Book>() {
                                                @Override
                                                public void handleResponse( Book response )
                                                {

                                                    Toast.makeText((Activity)context, "Order placed successfully!", Toast.LENGTH_SHORT).show();

                                                    dialog.dismiss();       // Dismissing the progressbar dialog before finishing the activity to prevent window leak
                                                    Log.i("backendless_order", "handleResponse: Book number has been updated");

                                                    // Show bkash txnid insert instruction
                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder((Activity)context);
                                                    alertDialogBuilder.setTitle(R.string.bkash);
                                                    alertDialogBuilder.setMessage(R.string.confirm_payment_bkash);
                                                    alertDialogBuilder.setPositiveButton("Okay",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface arg0, int arg1) {
                                                                    //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                                                    arg0.dismiss();
                                                                    ((Activity)context).finish();
                                                                }
                                                            });

                                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                                    alertDialog.show();

                                                }

                                                @Override
                                                public void handleFault( BackendlessFault fault )
                                                {
                                                    Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    Log.i("backendless_order", "handleFault: " + fault.getMessage());
                                                    // an error has occurred, the error code can be retrieved with fault.getCode()
                                                }
                                            } );
                                        }

                                        // Remaining Book Number Updated
                                        Log.i("backendless_order", "Remaining Book Number Updated");

                                        // Now reload the books
//                                        Intent returnIntent = new Intent();
//                                        ((Activity)context).setResult(Activity.RESULT_OK, returnIntent);

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                        Log.i("backendless_order", "handleFault: " + fault.getMessage());
                                    }
                                });


                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Log.i("backendless_order", "handleResponse: " + fault.getMessage());
                            }
                        });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Log.i("backendless_order", "handleFault: " + fault.getMessage());
            }
        });
    }

    public void updateOrderOnDatabase(final Context context, final Dialog dialog, final String updateCode){
        Backendless.Data.of( Order.class ).save( this, new AsyncCallback<Order>() {
            @Override
            public void handleResponse( Order updatedOrder )
            {
                // Contact instance has been updated
                if(updateCode.equals("bkash")){
                    Button button = ((Activity)context).findViewById(R.id.btnOrderDetails_SubmitBkash);
                    button.setVisibility(View.GONE);
                    EditText etBkash = ((Activity)context).findViewById(R.id.etOrderDetails_BkashTxnId);
                    etBkash.setCursorVisible(false);
                    etBkash.setFocusableInTouchMode(false);
                    etBkash.setClickable(false);
                    etBkash.setFocusable(false);
                }
                dialog.dismiss();
                Toast.makeText((Activity)context, "TxnId Submitted", Toast.LENGTH_SHORT).show();

            }
            @Override
            public void handleFault( BackendlessFault fault )
            {
                Toast.makeText((Activity)context, "Error occured while saving the data", Toast.LENGTH_SHORT).show();
                Log.e("update_order", "handleFault: " + fault.getMessage() + "\tCode: " + fault.getCode() );
                dialog.dismiss();
                // an error has occurred, the error code can be retrieved with fault.getCode()
            }
        } );
    }
}