package com.example.gronthomongol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class request extends AppCompatActivity implements AdapterView.OnItemClickListener {

    EditText etName;
    EditText etWriter;
    EditText etComment;
    Spinner spinner;
    Book requestedBook = new Book("বাংলা");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        setTitle("Request a Book");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_BookList);
        setSupportActionBar(toolbar);

        initializeGUIElements();
        initializeSpinner();         // Language Option Spinner initialization
        spinner.setOnItemClickListener(this);


    }

    private void initializeGUIElements(){
        etComment = findViewById(R.id.etRequest_Comment);
        etName = findViewById(R.id.etRequest_BookName);
        etWriter = findViewById(R.id.etRequest_WriterName);
        spinner = findViewById(R.id.spnRequest_language);
    }

    private void initializeSpinner(){
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        requestedBook.setLanguage((String) parent.getItemAtPosition(position));

        Log.i("bookreq", "Language Selected: " + requestedBook.getLanguage());
    }
}