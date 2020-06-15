package com.example.gronthomongol.backend;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.gronthomongol.R;
import com.example.gronthomongol.ui.auth.AuthActivity;

import java.util.ArrayList;
import java.util.List;

public class BackendlessAPIMethods {
    public static void logOut(final Context context, final Dialog dialog) {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Log.i("logout", "handleResponse: Device registration canceled");
                String channel;
                if((boolean) CONSTANTS.getCurrentUser().getProperty("admin")){
                    channel = "admin";
                }
                else{
                    channel = "client";
                }
                List<String> channelList = new ArrayList<String>(2);
                channelList.add(channel);
                Backendless.Messaging.unregisterDevice(channelList, new AsyncCallback<Integer>() {
                    @Override
                    public void handleResponse(Integer response) {
                        Toast.makeText(context, "সাইন আউট সফল হয়েছে!", Toast.LENGTH_SHORT).show();
                        Log.i("logout", "handleResponse: Device registration canceled");
                        dialog.dismiss();
                        Intent intent = new Intent(context, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        ((Activity)context).finish();
                        context.startActivity(intent);
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
                            Toast.makeText(context, "Sorry cannot logout right now", Toast.LENGTH_SHORT).show();
                        }
                        Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                    }
                });


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
                    Toast.makeText(context, "দুঃখিত, ইন্টারনেট সংযোগ নেই", Toast.LENGTH_SHORT).show();
                }
                Log.e("logout", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
            }
        });
    }
}
