package com.example.gronthomongol.ui.main.user.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.main.user.archive.BooklistActivity;
import com.example.gronthomongol.ui.util.adapters.CartAdapter;

import java.util.ArrayList;

import static com.example.gronthomongol.backend.CONSTANTS.orderedBooks;


public class BagFragment extends Fragment implements  View.OnClickListener{

    private EditText nameEditText;
    private EditText phoneNumberEditText;
    private EditText addressEditText;
    private TextView totalPriceEditText;
    private EditText commentEditText;
    private Button addMoreButton;
    private Button orderButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private CartAdapter cartAdapter;

    private int followedByProcessID;
    private String Recipient_Name;
    private String Contact_No;

    private int Total_Price = 0;    // Updated As soon as the activity starts. Check SetValues()
    private String Comment;
    private String Delivery_Address;

    private final String TAG = "placeOrder";

    public BagFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bag, container, false);

        orderedBooks = new ArrayList<Book>(CONSTANTS.getMaxNoOfBooksPerUserPerOrder());

        findXmlElements(view);
        setUpRecyclerView();
        setUpItemTouchHelper();

        orderedBooks.add((Book)  getActivity().getIntent().getSerializableExtra("selectedBook"));

        Log.i("placeorder", "ordered book array size = " + orderedBooks.size());

        followedByProcessID = getActivity().getIntent().getIntExtra("ID",0);

        setFields();

        setUpListeners();

        return view;
    }

    private void findXmlElements(View view){
        nameEditText = view.findViewById(R.id.nameEditTextBag);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditTextBag);
        addressEditText = view.findViewById(R.id.addressEditTextBag);
        totalPriceEditText = view.findViewById(R.id.totalPriceTextViewBag);
        commentEditText = view.findViewById(R.id.commentEditTextBag);
        recyclerView = view.findViewById(R.id.recyclerViewBag);
        addMoreButton = view.findViewById(R.id.addMoreButtonBag);
        orderButton = view.findViewById(R.id.orderButtonBag);
    }

    private void setUpRecyclerView() {
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        cartAdapter = new CartAdapter(orderedBooks, getContext());
        recyclerView.setAdapter(cartAdapter);
    }

    private void setUpItemTouchHelper(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(getContext(), "on Move", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Toast.makeText(placeOrder.this, "on Swiped ", Toast.LENGTH_SHORT).show();
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                // Update Total_Price
                Total_Price = Total_Price - orderedBooks.get(position).getPrice();
                totalPriceEditText.setText(Integer.toString(Total_Price));
                orderedBooks.remove(position);
                cartAdapter.notifyDataSetChanged();
                addMoreButton.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "বইটি রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setFields(){
        nameEditText.setText((CharSequence) Backendless.UserService.CurrentUser().getProperty("name"));
        Contact_No = (String) CONSTANTS.getCurrentUser().getProperty("contact_no");
        if(Contact_No.equals(CONSTANTS.NULLMARKER)){}
        else {
            phoneNumberEditText.setText((CharSequence) CONSTANTS.getCurrentUser().getProperty("contact_no"));
        }
        Total_Price = orderedBooks.get(0).getPrice();   // Total Price Updated
        totalPriceEditText.setText(Integer.toString(Total_Price));
    }

    private void setUpListeners(){
        addMoreButton.setOnClickListener(this);
        orderButton.setOnClickListener(this);
    }

    private boolean isEmpty(EditText editText){
        if(editText.getText().toString().isEmpty()){
            editText.setError("Please enter a subject");
            return true;
        }
        return false;
    }

    private void getValues(){
        Recipient_Name = nameEditText.getText().toString().trim();
        Contact_No = phoneNumberEditText.getText().toString().trim();
        Total_Price = Integer.parseInt(totalPriceEditText.getText().toString());
        Comment = commentEditText.getText().toString().trim();
        Delivery_Address = addressEditText.getText().toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // User Clicked on Add More Book and have come back after Selecting a new Book
        if(requestCode == CONSTANTS.getIdPlaceOrderAddMoreBook()){
            if(resultCode == Activity.RESULT_OK){

                Book newBook = (Book) data.getSerializableExtra(getString(R.string.newlySelectedBook));
                orderedBooks.add(newBook);
                cartAdapter.notifyDataSetChanged();
                // Update Total Price
                Total_Price = Total_Price + newBook.getPrice();
                totalPriceEditText.setText(Integer.toString(Total_Price));
                if(orderedBooks.size() == CONSTANTS.getMaxNoOfBooksPerUserPerOrder()) {
                    Toast.makeText(getContext(), "দুঃখিত, সর্বোচ্চ ১০ টি বই অর্ডার করা যাবে", Toast.LENGTH_SHORT).show();
                    addMoreButton.setVisibility(View.INVISIBLE);
                }
            }
        }

    }


    @Override
    public void onClick(View view) {
        if(view==addMoreButton){
            Intent intent = new Intent(getContext(), BooklistActivity.class);
            intent.putExtra(getString(R.string.activityIDName), CONSTANTS.getIdPlaceOrderAddMoreBook());
            startActivityForResult(intent, CONSTANTS.getIdPlaceOrderAddMoreBook());
        } else if(view == orderButton){
            if(!isEmpty(nameEditText) && !isEmpty(phoneNumberEditText) && !isEmpty(addressEditText)){
                // First Check if orderedBookList is empty or not
                if(orderedBooks.size() == 0){
                    Toast.makeText(getContext(), getString(R.string.add_atleast_one_book), Toast.LENGTH_LONG).show();
                }
                else {

                    final Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialog_loading);
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
                            newOrder.saveOrderInBackendless(orderedBooks, getContext(), dialog);
                        }
                    });

                    thread.start();
                }
            }
        }
    }
}