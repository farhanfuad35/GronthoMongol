package com.example.gronthomongol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;

public class Request {
    private String objectId;
    private String bookName;
    private String writerName;
    private BackendlessUser requestingUser;
    private String language;
    private String comment;
    private boolean resolved;

    public Request(String bookName, String writerName, BackendlessUser requestingUser, String language, String comment, boolean resolved) {
        this.bookName = bookName;
        this.writerName = writerName;
        this.requestingUser = requestingUser;
        this.language = language;
        if(comment.isEmpty())
            this.comment = CONSTANTS.NULLMARKER;
        else
            this.comment = comment;
        this.resolved = resolved;
    }


    public void saveRequestInBackendless(final Context context, final Dialog dialog){
        final ArrayList<BackendlessUser> requestingUser = new ArrayList<>(1);
        requestingUser.add(CONSTANTS.getCurrentUser());

        Backendless.Data.of(Request.class).save(this, new AsyncCallback<Request>() {
            @Override
            public void handleResponse(Request savedRequest) {
                Log.i("save_request", "handleResponse: Request saved in database");
                Backendless.Data.of(Request.class).setRelation(savedRequest, "requestingUser", requestingUser, new AsyncCallback<Integer>() {
                    @Override
                    public void handleResponse(Integer response) {
                        Log.i("save_request", "handleResponse: user relation set with the request successfully");
                        Toast.makeText((Activity)context, "Request Submitted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        ((Activity)context).finish();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText((Activity)context, "Request Submission Failed!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Log.i("save_request", "handleFault: " + fault.getMessage());
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                dialog.dismiss();
                if(fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless))){
                    CONSTANTS.showConnectionFailedDialogWithoutRestart((Activity)context);
                }
                else{
                    Toast.makeText((Activity)context, "Request Submission Failed!", Toast.LENGTH_SHORT).show();
                }
                Log.i("save_request", "handleFault: " + fault.getMessage());
            }
        });
    }

    public Request(){
        comment = CONSTANTS.NULLMARKER;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getBookName() {
        return bookName;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public BackendlessUser getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(BackendlessUser requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
