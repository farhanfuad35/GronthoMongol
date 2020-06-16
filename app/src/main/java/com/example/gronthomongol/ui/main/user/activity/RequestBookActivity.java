package com.example.gronthomongol.ui.main.user.activity;

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
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Request;
import com.example.gronthomongol.backend.CONSTANTS;

public class RequestBookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private EditText nameEditText;
    private EditText authorEditText;
    private Spinner languageSpinner;
    private TextView errorTextView;
    private Button backButton;
    private Button requestButton;

    ArrayAdapter<CharSequence> adapter;
    private String name;
    private String author;
    private String language;

    private boolean resolved;
    private BackendlessUser requestingUser;
    private String TAG = "postRequest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_book);

        findXmlElements();
        setUpSpinner();         // Language Option Spinner initialization
        setUpListeners();
    }

    private void findXmlElements(){
        nameEditText = findViewById(R.id.nameEditTextRequestBook);
        authorEditText = findViewById(R.id.authorEditTextRequestBook);
        languageSpinner = findViewById(R.id.languageSpinnerRequestBook);
        errorTextView = findViewById(R.id.errorTextViewRequestBook);
        backButton = findViewById(R.id.backButtonRequestBook);
        requestButton = findViewById(R.id.requestButtonRequestBook);
    }

    private void setUpSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        languageSpinner.setAdapter(adapter);
    }


    private void setUpListeners(){
        languageSpinner.setOnItemSelectedListener(RequestBookActivity.this);
        backButton.setOnClickListener(this);
        requestButton.setOnClickListener(this);
    }

    private void getValues(){
        name = nameEditText.getText().toString().trim();
        author = authorEditText.getText().toString().trim();
        requestingUser = CONSTANTS.getCurrentUser();
        resolved = false;
    }

    private boolean isEmpty(EditText editText){
        if(editText.getText().toString().isEmpty()){
            editText.setError("This field cannot be empty!");
            return true;
        }
        return false;
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

    @Override
    public void onClick(View view) {
        if(view == requestButton){
            // TODO
            if(!isEmpty(nameEditText) && !isEmpty(authorEditText)) {
                getValues();
                final Request request = new Request(name, author, requestingUser, language, resolved);
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
        } else if(view == backButton){

        }
    }
}