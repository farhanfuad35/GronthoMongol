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

public class AdminBooksAdapter extends RecyclerView.Adapter<AdminBooksAdapter.BookItemViewHolder> {
    private Context context;
    private List<Book> books;
    private OnBookClickListener mOnBookClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class BookItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView nameTextView;
        public TextView authorTextView;
        public TextView priceTextView;
        public TextView countTextView;
        OnBookClickListener onBookClickListener;

        public BookItemViewHolder(View view, OnBookClickListener onBookClickListener) {
            super(view);
            this.onBookClickListener = onBookClickListener;
            countTextView = view.findViewById(R.id.countTextViewItemAdminBook);
            nameTextView = view.findViewById(R.id.nameTextViewItemAdminBook);
            authorTextView = view.findViewById(R.id.authorTextViewItemAdminBook);
            priceTextView = view.findViewById(R.id.priceTextViewItemAdminBook);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBookClickListener.onBookClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdminBooksAdapter(List<Book> books_para, Context context, OnBookClickListener onBookClickListener) {
        mOnBookClickListener = onBookClickListener;
        books = books_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdminBooksAdapter.BookItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_books, parent, false);
        BookItemViewHolder vh = new BookItemViewHolder(view, mOnBookClickListener);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(BookItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String count = Integer.toString(books.get(position).getQuantity());
        String name = books.get(position).getName();
        String author = books.get(position).getWriter();
        String price = books.get(position).getPrice() + " BDT";

        holder.countTextView.setText(count);
        holder.nameTextView.setText(name);
        holder.authorTextView.setText(author);
        holder.priceTextView.setText(price);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return books.size();
    }

    public interface OnBookClickListener{
        void onBookClick(int position);
    }
}