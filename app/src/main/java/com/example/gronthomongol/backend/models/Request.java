package com.example.gronthomongol.backend.models;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.ui.util.adapters.RequestsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Request {
    private String objectId;
    private String bookName;
    private String writerName;
    private BackendlessUser requestingUser;
    private String language;
    private boolean resolved;

    public Request(String bookName, String writerName, BackendlessUser requestingUser, String language, boolean resolved) {
        this.bookName = bookName;
        this.writerName = writerName;
        this.requestingUser = requestingUser;
        this.language = language;
        this.resolved = resolved;
    }


    public void saveRequestInBackendless(final Context context, final Dialog dialog){
        final ArrayList<BackendlessUser> requestingUser = new ArrayList<>(1);
        requestingUser.add(CONSTANTS.getCurrentUser());

        Backendless.Data.of(Request.class).save(this, new AsyncCallback<Request>() {
            @Override
            public void handleResponse(final Request savedRequest) {
                Log.i("save_request", "handleResponse: Request saved in database");
                Backendless.Data.of(Request.class).setRelation(savedRequest, "requestingUser", requestingUser, new AsyncCallback<Integer>() {
                    @Override
                    public void handleResponse(Integer response) {
                        Log.i("save_request", "handleResponse: user relation set with the request successfully");


                        // Send request notification to the admins
                        String message = CONSTANTS.getCurrentUser().getProperty("name") + " has requested a new book: " + savedRequest.getBookName() + " by " + savedRequest.getWriterName();
                        String messageTitle = "New book request!";
                        CONSTANTS.sendNotificationToTheAdmins(message, messageTitle);

                        dialog.dismiss();
                        Toast.makeText((Activity)context, "Request Submitted!", Toast.LENGTH_SHORT).show();
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
                        else
                            Toast.makeText((Activity)context, "Request Submission Failed!", Toast.LENGTH_SHORT).show();

                        Log.i("save_request", "handleFault: " + fault.getMessage());
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
                else{
                    Toast.makeText((Activity)context, "Request Submission Failed!", Toast.LENGTH_SHORT).show();
                }
                Log.i("save_request", "handleFault: " + fault.getMessage());
            }
        });
    }

    public Request(){
    }


    @Override
    public boolean equals(Object request) {
        if (!(request instanceof Request)) {
            return false;
        }

        Request thatRequest = (Request) request;

        // Custom equality check here.
        return this.objectId.equals(thatRequest.objectId);
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

    public void markAsResolvedAndNotify(final Context context, final Dialog dialog, final RequestsAdapter requestlistAdapterRV){
        final Request resolvingRequest = this;
        resolvingRequest.setResolved(true);
        Backendless.Data.of(Request.class).save(resolvingRequest, new AsyncCallback<Request>() {
            @Override
            public void handleResponse(Request response) {
                final String message = resolvingRequest.bookName + " by " + resolvingRequest.getWriterName() + " is now available in our stock";
                final String messageTitle = "Order Your Requested Book!";
                // First retrieve the device id of the requesting user
                DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
                dataQueryBuilder.setWhereClause("user.objectId = '" + resolvingRequest.getRequestingUser().getObjectId() + "'");
                dataQueryBuilder.addProperty("deviceId");

                Log.i("request_notification", "handleResponse: requesting User objectId: " + resolvingRequest.getRequestingUser().getObjectId());

                Backendless.Data.of("DeviceRegistration").find(dataQueryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        Log.i("request_notification", "handleResponse_Mapping: Notification sent to the requesting user");

                        for(Map device: response){
                            Log.i("request_list", (String) device.get("deviceId"));
                        }

                        if(!response.isEmpty()) {

                            DeliveryOptions deliveryOptions = new DeliveryOptions();
                            deliveryOptions.setPushSinglecast(Arrays.asList((String) response.get(0).get("deviceId")));


                            PublishOptions publishOptions = new PublishOptions();
                            publishOptions.putHeader("android-ticker-text", "You just got a private push notification!");
                            publishOptions.putHeader("android-content-title", messageTitle);
                            publishOptions.putHeader("android-content-text", "Push Notifications Text");
                            Backendless.Messaging.publish("client", message, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                                @Override
                                public void handleResponse(MessageStatus response) {
                                    for (int i = 0; i < CONSTANTS.userRequestsCached.size(); i++) {
                                        if (CONSTANTS.userRequestsCached.get(i).equals(resolvingRequest)) {
                                            CONSTANTS.userRequestsCached.remove(i);
                                            requestlistAdapterRV.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    Toast.makeText(((Activity) context), "Sent notification to the user", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    Log.i("request_notification", "handleResponse: Notification sent to the requesting user");
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    dialog.dismiss();
                                    String title;
                                    String message;
                                    if (fault.getMessage().equals(((Activity) context).getString(R.string.connectionErrorMessageBackendless))) {
                                        title = "Connection Failed!";
                                        message = "Please Check Your Internet Connection";
                                        CONSTANTS.showErrorDialog((Activity) context, title, message, "Okay", null, 0);
                                    } else {
                                        Toast.makeText((Activity) context, "Error occured while saving the data", Toast.LENGTH_SHORT).show();
                                    }
                                    Log.i("request_notification", "handleFault: Notification sending to the requesting user failed\t" + fault.getMessage());
                                }
                            });
                        }
                        else {
                            Log.i("request_notification", "retrieved device id for sending notification was empty");
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.i("request_notification", "handleResponse_Mapping: error " + fault.getMessage());
                        dialog.dismiss();
                        String title;
                        String message;
                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            CONSTANTS.showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                        }
                        else{
                            Toast.makeText((Activity)context, "Error occurred while saving the data", Toast.LENGTH_SHORT).show();
                            Log.i("request_notification", "handleFault: couldn't find requesting user's deviceid\t" + fault.getMessage());
                        }
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
                else{
                    Log.i("request_notification", "handleFault: Couldn't save the request as resolved\t" + fault.getMessage());
                    Toast.makeText((Activity)context, "Error occurred while updating the request", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void removeRequest(final Context context, final Dialog dialog, final RequestsAdapter requestlistAdapterRV){
        final Request deletingRequest = this;
        Backendless.Data.of(Request.class).remove(this, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                dialog.dismiss();
                Log.i("request_delete", "handleResponse: The request was deleted successfully");
                Toast.makeText(((Activity)context), "The request was deleted", Toast.LENGTH_SHORT).show();
                for(int i=0; i < CONSTANTS.userRequestsCached.size() ; i++){
                    if(CONSTANTS.userRequestsCached.get(i).equals(deletingRequest)){
                        CONSTANTS.userRequestsCached.remove(i);
                        requestlistAdapterRV.notifyDataSetChanged();
                        break;
                    }
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
                else{
                    Log.i("request_notification", "handleFault: Couldn't save the request as resolved\t" + fault.getMessage());
                    Toast.makeText((Activity)context, "Error occured while deleting the request", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
