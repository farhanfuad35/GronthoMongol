package com.example.gronthomongol.ui.auth.test;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gronthomongol.R;

public class ForgotPasswordFragment extends Fragment implements View.OnClickListener{



    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);










        return view;
    }

    @Override
    public void onClick(View view) {

    }
}