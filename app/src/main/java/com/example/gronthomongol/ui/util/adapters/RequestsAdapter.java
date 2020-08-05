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

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestItemViewHolder> {
    private Context context;
    private List<Request> requests;
    private OnRequestClickListener monRequestClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class RequestItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        private TextView nameTextView;
        private TextView authorTextView;
        OnRequestClickListener onRequestClickListener;

        public RequestItemViewHolder(View v, OnRequestClickListener onRequestClickListener) {
            super(v);
            this.onRequestClickListener = onRequestClickListener;
            nameTextView = v.findViewById(R.id.nameTextViewItemRequest);
            authorTextView = v.findViewById(R.id.authorTextViewItemRequest);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRequestClickListener.onRequestClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RequestsAdapter(List<Request> requests_para, Context context, OnRequestClickListener onRequestClickListener) {
        monRequestClickListener = onRequestClickListener;
        requests = requests_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RequestsAdapter.RequestItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_requests, parent, false);
        RequestItemViewHolder viewHolder = new RequestItemViewHolder(view, monRequestClickListener);
        return viewHolder;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(RequestItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        DateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy, EEE");
        Request request = requests.get(position);
        String name = request.getBookName();
        String author = request.getWriterName();

        holder.nameTextView.setText(name);
        holder.authorTextView.setText(author);
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