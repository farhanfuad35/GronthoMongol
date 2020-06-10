package com.example.gronthomongol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.Serializable;
import java.util.List;

public class Book implements Serializable {
    private String objectId;
    private String name;
    private String writer;
    private int price;
    private int quantity;
    private String language;

//    public Book(String language) {
//        this.language = language;
//    }


    @Override
    public boolean equals(Object book) {
        if (!(book instanceof Book)) {
            return false;
        }

        Book thatBook = (Book) book;

        // Custom equality check here.
        return this.objectId.equals(thatBook.objectId);
    }

    public Book get(){
        return this;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public final void deleteBook(final Context context, final Dialog dialog){
        final Book bookToBeDeleted = this;
        Backendless.Data.of(Book.class).remove(this, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                // Delete it from the current list
                Log.i("book_deletion", "handleResponse: list size before deletion: " + CONSTANTS.getBookListCached().size());
                Log.i("book_deletion", "handleResponse: received book to be deleted oId: " + bookToBeDeleted.getObjectId());
                if(CONSTANTS.isShowingDefaultBooklist()) {
                    deleteIfFound(CONSTANTS.bookListCached, bookToBeDeleted);
                }
                else{
                    deleteIfFound(CONSTANTS.tempBookListCached, bookToBeDeleted);
                }
                dialog.dismiss();
                Toast.makeText((Activity)context, "The book has been deleted successfully", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("info", "Book Deleted");
                ((Activity)context).setResult(Activity.RESULT_OK, returnIntent);
                ((Activity)context).finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                dialog.dismiss();
                String title;
                String message;
                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                }
                else {
                    Toast.makeText((Activity) context, "Sorry the book couldn't be deleted at this moment", Toast.LENGTH_SHORT).show();
                }
                Log.i("book_deletion", "handleFault: " + fault.getMessage());
            }
        });
    }

    public void saveBook(final Context context, final Dialog dialog, final boolean savingBook){
        final Book bookToBeSaved = this;
        Backendless.Data.of(Book.class).save(this, new AsyncCallback<Book>() {
            @Override
            public void handleResponse(Book response) {

                dialog.dismiss();
                if(!savingBook) {
                    // Replace the book only if it is being UPDATED, not CREATED
                    // Created book is added through a fresh retrieve because of different active sort mode
                    if(CONSTANTS.isShowingDefaultBooklist()) {
                        if(updateBookIfFound(CONSTANTS.bookListCached, bookToBeSaved, response))
                            sortBookList(context, CONSTANTS.bookListCached);
                    }
                    else{
                        if(updateBookIfFound(CONSTANTS.tempBookListCached, bookToBeSaved, response))
                            sortBookList(context, CONSTANTS.tempBookListCached);
                    }


                    Toast.makeText(((Activity) context), "Book updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.i("book_update", "handleResponse: Book has been successfully updated");
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("info", "book_updated");
                    ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
                    ((Activity) context).finish();
                }
                else{
                    Toast.makeText(((Activity)context), "Book saved in the database successfully", Toast.LENGTH_SHORT).show();
                    Log.i("book_save", "handleResponse: Book saved in the database");
                    ((Activity)context).finish();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                dialog.dismiss();
                String title;
                String message;
                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    CONSTANTS.showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                }
                else {
                    Toast.makeText((Activity)context, "The book couldn't be updated at this moment", Toast.LENGTH_SHORT).show();
                }
                Log.i("book_update", "handleFault: Book couldn't be updated\t" + fault.getMessage());
            }
        });
    }

    private boolean deleteIfFound(List<Book> currentBooks, Book bookToBeDeleted){
        for (int i = 0; i < currentBooks.size(); i++) {
            Log.i("book_deletion", "traversed book " + i + ": oId: " + CONSTANTS.getBookListCached().get(i).getObjectId());
            if (CONSTANTS.getBookListCached().get(i).equals(bookToBeDeleted)) {
                CONSTANTS.getBookListCached().remove(i);
                Log.i("book_deletion", "handleResponse: found a matched book. current size: " + CONSTANTS.getBookListCached().size());
                return true;
            }
        }
        return false;
    }

    private boolean updateBookIfFound(List<Book> currentBooks, Book oldBook, Book bookToBeSaved){
        for (int i = 0; i < currentBooks.size(); i++) {
            if (currentBooks.get(i).equals(oldBook)) {
                currentBooks.remove(i);
                currentBooks.add(i, bookToBeSaved);
                return true;
            }
        }
        return false;
    }

    private void sortBookList(Context context, List<Book> bookListToBeSorted){
        SharedPreferences pref = ((Activity)context).getSharedPreferences("preferences", 0); // 0 - for private mode
        String sortBy = pref.getString("sortBy", "name");

        if(sortBy.equals("name")){
            asfasf
        }
        else if(sortBy.equals("writer")){

        }
        else if(sortBy.equals("quantity")){

        }
    }
}
