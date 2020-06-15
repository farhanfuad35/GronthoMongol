package com.example.gronthomongol.ui.util.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Request;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class RequestlistAdapterRV extends RecyclerView.Adapter<RequestlistAdapterRV.MyViewHolder> {
    private Context context;
    private List<Request> requests;
    private OnRequestClickListener monRequestClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        private TextView tvBookname;
        private TextView tvWriter;
        OnRequestClickListener onRequestClickListener;
        public MyViewHolder(View v, OnRequestClickListener onRequestClickListener) {
            super(v);
            this.onRequestClickListener = onRequestClickListener;
            tvBookname = v.findViewById(R.id.tvRequestRow_BookName);
            tvWriter = v.findViewById(R.id.tvRequestRow_Writer);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRequestClickListener.onRequestClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RequestlistAdapterRV(List<Request> requests_para, Context context, OnRequestClickListener onRequestClickListener) {
        monRequestClickListener = onRequestClickListener;
        requests = requests_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RequestlistAdapterRV.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_request_list_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(view, monRequestClickListener);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        DateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy, EEE");
        Request request = requests.get(position);

        holder.tvBookname.setText(request.getBookName() + " | " + request.getLanguage());
        holder.tvWriter.setText(request.getWriterName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return requests.size();
    }

    public interface OnRequestClickListener{
        void onRequestClick(int position);
    }
}