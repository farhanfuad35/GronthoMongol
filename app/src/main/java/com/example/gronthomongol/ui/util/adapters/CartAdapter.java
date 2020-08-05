package com.example.gronthomongol.ui.util.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Book;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemViewHolder> {
    private Context context;
    private List<Book> books;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CartItemViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView nameTextView;
        public TextView authorTextView;
        public TextView priceTextView;
        public CartItemViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.nameTextViewItemCart);
            authorTextView = view.findViewById(R.id.authorTextViewItemCart);
            priceTextView = view.findViewById(R.id.priceTextViewItemCart);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CartAdapter(List<Book> books_para, Context context) {
        books = books_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CartAdapter.CartItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        CartItemViewHolder viewHolder = new CartItemViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CartItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String name = books.get(position).getName();
        String author = books.get(position).getWriter();
        String price = books.get(position).getPrice() + " BDT";

        holder.nameTextView.setText(name);
        holder.authorTextView.setText(author);
        holder.priceTextView.setText(price);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return books.size();
    }

}