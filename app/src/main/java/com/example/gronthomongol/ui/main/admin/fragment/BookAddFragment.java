package com.example.gronthomongol.ui.main.admin.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.ui.main.admin.archive.AddBookActivity;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

public class BookAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    private EditText nameEditText;
    private EditText authorEditText;
    private EditText priceEditText;
    private ScrollableNumberPicker countNumberPicker;
    private Spinner languageSpinner;
    private Button addButton;

    private ArrayAdapter<CharSequence> adapter;
    private Book newBook = new Book();
    private String name;
    private String author;
    private int price;
    private int quantity = 1;
    private String language;

    public BookAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_add, container, false);

        findXmlElements(view);
        setUpNumberPicker();
        setUpListeners();

        return view;
    }

    private void findXmlElements(View view) {
        nameEditText = view.findViewById(R.id.nameEditTextBookAdd);
        authorEditText = view.findViewById(R.id.authorEditTextBookAdd);
        priceEditText = view.findViewById(R.id.priceEditTextBookAdd);
        languageSpinner = view.findViewById(R.id.languageSpinnerBookAdd);
        countNumberPicker = view.findViewById(R.id.countNumberPickerBookAdd);
        addButton = view.findViewById(R.id.addButtonBookAdd);
    }

    private void setUpNumberPicker(){
        countNumberPicker.setListener(new ScrollableNumberPickerListener() {
            @Override
            public void onNumberPicked(int value) {
                quantity = value;
            }
        });
    }

    private void setUpListeners(){
        languageSpinner.setOnItemSelectedListener(this);
        addButton.setOnClickListener(this);
    }

    private boolean gotValues(){
        name = nameEditText.getText().toString().trim();
        if(name.isEmpty()) {
            nameEditText.setError("বইয়ের নাম সঠিক নয়");
            return false;
        }

        author = authorEditText.getText().toString().trim();
        if(author.isEmpty()){
            authorEditText.setError("লেখকের নাম সঠিক নয়");
            return false;
        }

        String priceStr = priceEditText.getText().toString();
        if(priceStr.isEmpty()){
            priceEditText.setError("মূল্য সঠিক নয়");
            return false;
        } else{
            try{
                price = Integer.parseInt(priceStr);
            }catch (NumberFormatException nfe){
                priceEditText.setError("মূল্য সঠিক নয়");
                return false;
            }
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        language = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        if(view == addButton){
            handleAdd();
        }
    }

    private void handleAdd(){
        if(gotValues()){
            newBook.setName(name);
            newBook.setWriter(author);
            newBook.setPrice(price);
            newBook.setLanguage(language);
            newBook.setQuantity(quantity);

            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_adding_book);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    newBook.saveBook(getContext(), dialog, true);
                }
            });

            thread.start();
        }
    }
}