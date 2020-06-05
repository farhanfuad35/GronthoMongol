package com.example.gronthomongol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceOrderAdapterRV extends RecyclerView.Adapter<PlaceOrderAdapterRV.MyViewHolder> {
    private Context context;
    private List<Book> books;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView tvBookName;
        public TextView tvWriterName;
        public TextView tvPrice;
        public MyViewHolder(View v) {
            super(v);
            tvBookName = v.findViewById(R.id.tvRow_BookName);
            tvWriterName = v.findViewById(R.id.tvRow_Writer);
            tvPrice = v.findViewById(R.id.tvBookList_price);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlaceOrderAdapterRV(List<Book> books_para, Context context) {
        books = books_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlaceOrderAdapterRV.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_place_order_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tvBookName.setText(books.get(position).getName() + " | " + books.get(position).getLanguage());
        holder.tvWriterName.setText(books.get(position).getWriter());
        holder.tvPrice.setText(books.get(position).getPrice() + "/=");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return books.size();
    }

}