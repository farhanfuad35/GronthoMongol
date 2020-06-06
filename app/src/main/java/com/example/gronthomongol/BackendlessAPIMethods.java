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

public class BackendlessAPIMethods {
    public static void logOut(final Context context, final Dialog dialog) {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ((Activity)context).finish();
                context.startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                dialog.dismiss();
                Log.e("logout", "handleFault: " + fault.getCode() + "\t" + fault.getMessage());
                Toast.makeText(context, "Sorry couldn't logout right now. Please check your connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateDeviceId(Context context, BackendlessUser user, String deviceId) {

        //BackendlessUser user = Backendless.UserService.CurrentUser();

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
                Log.i("deviceid", backendlessFault.getMessage());
            }
        });
    }


}
