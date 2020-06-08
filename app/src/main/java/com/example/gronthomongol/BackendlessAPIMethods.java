package com.example.gronthomongol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

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
                        Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_SHORT).show();
                        Log.i("logout", "handleResponse: Device registration canceled");
                        dialog.dismiss();
                        Intent intent = new Intent(context, login.class);
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
                            CONSTANTS.showErrorDialog((Activity)context, title, message, "Okay");
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
                    CONSTANTS.showErrorDialog((Activity)context, title, message, "Okay");
                }
                else {
                    Toast.makeText(context, "Sorry couldn't logout right now. Please check your connection", Toast.LENGTH_SHORT).show();
                }
                Log.e("logout", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
            }
        });
    }
//
//    public static void logOutFromSplashScreen(final Context context) {
//        Backendless.UserService.logout(new AsyncCallback<Void>() {
//            @Override
//            public void handleResponse(Void response) {
//                Log.i("logout", "handleResponse: Device registration canceled");
//                String channel;
//                if((boolean) CONSTANTS.getCurrentUser().getProperty("admin")){
//                    channel = "admin";
//                }
//                else{
//                    channel = "client";
//                }
//                List<String> channelList = new ArrayList<String>(2);
//                channelList.add(channel);
//                Backendless.Messaging.unregisterDevice(channelList, new AsyncCallback<Integer>() {
//                    @Override
//                    public void handleResponse(Integer response) {
//                        Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_SHORT).show();
//                        Log.i("logout", "handleResponse: Device registration canceled");
//                        Intent intent = new Intent(context, login.class);
//                        ((Activity)context).finish();
//                        context.startActivity(intent);
//                    }
//
//                    @Override
//                    public void handleFault(BackendlessFault fault) {
//                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
//                            CONSTANTS.showErrorDialog_splashScreen((Activity)context, "Connection Failed", "Please Check Your Internet Connection",
//                                    "Retry", "Quit");
//                        else {
//                            Intent intent = new Intent(context, login.class);
//                            ((Activity)context).finish();
//                            context.startActivity(intent);
//                            Toast.makeText(context, "Sorry cannot logout right now", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//
//            }
//
//            @Override
//            public void handleFault(BackendlessFault fault) {
//                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
//                    CONSTANTS.showErrorDialog_splashScreen((Activity)context, "Connection Failed", "Please Check Your Internet Connection",
//                            "Retry", "Quit");
//                else {
//                    Intent intent = new Intent(context, login.class);
//                    ((Activity)context).finish();
//                    context.startActivity(intent);
//                    Toast.makeText(context, "Sorry cannot logout right now", Toast.LENGTH_SHORT).show();
//                }
//                Log.e("logout", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
//            }
//        });
//    }

}
