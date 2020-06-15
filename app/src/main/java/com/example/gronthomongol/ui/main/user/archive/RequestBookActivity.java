package com.example.gronthomongol.ui.main.user.archive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.backendless.BackendlessUser;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Request;
import com.example.gronthomongol.backend.CONSTANTS;

public class RequestBookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    EditText etName;
    EditText etWriter;
    Spinner spinner;
    Button btnRequestBook;
    ArrayAdapter<CharSequence> adapter;

    private String bookName;
    private String writerName;
    private String language;
    private String comment;
    private boolean resolved;
    private BackendlessUser requestingUser;

    private String TAG = "postRequest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_book);


        setTitle("Request a Book");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_Request);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeGUIElements();
        initializeSpinner();         // Language Option Spinner initialization
        spinner.setOnItemSelectedListener(RequestBookActivity.this);
        btnRequestBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                if(!isEmpty(etName) && !isEmpty(etWriter)) {
                    getValues();
                    final Request request = new Request(bookName, writerName, requestingUser, language, resolved);
                    final Dialog dialog = new Dialog(RequestBookActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialog_sending_request);
                    dialog.show();

                    Log.i(TAG, "onClick: request object Created");

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            request.saveRequestInBackendless(RequestBookActivity.this, dialog);
                        }
                    });

                    thread.start();
                }
            }
        });


    }

    private void initializeGUIElements(){
        etName = findViewById(R.id.etRequest_BookName);
        etWriter = findViewById(R.id.etRequest_WriterName);
        spinner = findViewById(R.id.spnRequest_language);
        btnRequestBook = findViewById(R.id.btnRequest_Post);
    }

    private boolean isEmpty(EditText editText){
        if(editText.getText().toString().isEmpty()){
            editText.setError("This field cannot be empty!");
            return true;
        }
        return false;
    }

    private void initializeSpinner(){
// Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void getValues(){
        bookName = etName.getText().toString().trim();
        writerName = etWriter.getText().toString().trim();
        requestingUser = CONSTANTS.getCurrentUser();
        resolved = false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        language = parent.getItemAtPosition(position).toString();

        Log.i("bookreq", "Language Selected: " + language);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}