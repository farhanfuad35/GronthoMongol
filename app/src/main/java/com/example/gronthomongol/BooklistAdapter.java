package com.example.gronthomongol;

// 'books' is a List
// DONE


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BooklistAdapter extends ArrayAdapter<Book> {

    private Context context;
    private List<Book> books;

    public BooklistAdapter(@NonNull Context context, List<Book> books_para) {        // 'books' is a List
        super(context, R.layout.row_book_list_layout, books_para);
        this.context = context;
        books = books_para;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.row_book_list_layout, parent, false);

        TextView tvBookName = convertView.findViewById(R.id.tvRow_BookName);
        TextView tvWriter = convertView.findViewById(R.id.tvRow_Writer);

        tvBookName.setText(books.get(position).getName());
        tvWriter.setText(books.get(position).getWriter());

        return convertView;
    }
}
