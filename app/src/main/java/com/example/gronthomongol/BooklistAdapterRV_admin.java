package com.example.gronthomongol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BooklistAdapterRV_admin extends RecyclerView.Adapter<BooklistAdapterRV_admin.MyViewHolder> {
    private Context context;
    private List<Book> books;
    private OnBookClickListener monBookClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView tvBookName;
        public TextView tvWriterName;
        public TextView tvPrice;
        public TextView tvNumberOfBooks;
        OnBookClickListener onBookClickListener;
        public MyViewHolder(View v, OnBookClickListener onBookClickListener) {
            super(v);
            this.onBookClickListener = onBookClickListener;
            tvBookName = v.findViewById(R.id.tvRow_BookName);
            tvWriterName = v.findViewById(R.id.tvRow_Writer);
            tvPrice = v.findViewById(R.id.tvBookList_price);
            tvNumberOfBooks = v.findViewById(R.id.tvBookList_admin_numberOfBooks);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBookClickListener.onBookClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BooklistAdapterRV_admin(List<Book> books_para, Context context, OnBookClickListener onBookClickListener) {
        monBookClickListener = onBookClickListener;
        books = books_para;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BooklistAdapterRV_admin.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_book_list_admin_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(view, monBookClickListener);
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
        holder.tvNumberOfBooks.setText(Integer.toString(books.get(position).getQuantity()));

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