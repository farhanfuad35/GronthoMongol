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

import static com.example.gronthomongol.CONSTANTS.showConnectionFailedDialog;
import static com.example.gronthomongol.CONSTANTS.showConnectionFailedDialogWithoutRestart;

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
                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
                            showConnectionFailedDialogWithoutRestart(context);
                        else {
                            Toast.makeText(context, "Sorry cannot logout right now", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }

            @Override
            public void handleFault(BackendlessFault fault) {
                dialog.dismiss();
                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
                    showConnectionFailedDialogWithoutRestart(context);
                else {
                    Toast.makeText(context, "Sorry couldn't logout right now. Please check your connection", Toast.LENGTH_SHORT).show();
                }
                Log.e("logout", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
            }
        });
    }

    public static void updateDeviceId(final Context context, BackendlessUser user, String deviceId) {

        Log.i("deviceid", "before get email");
        //Log.i("deviceid", "email of CurrentUser : " + user.getEmail());
        Log.i("deviceid", "after get email");

        user.setProperty("device_id", deviceId);

        Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                Log.i("deviceid", "User has been updated");
                Log.i("deviceid", "Device ID - " + backendlessUser.getProperty("device_id"));
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                if( backendlessFault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) ))
                    showConnectionFailedDialogWithoutRestart(context);

                Log.i("deviceid", backendlessFault.getMessage());
            }
        });
    }


}
