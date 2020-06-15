/*  Orderlist Sort must be by last created. Because after creating a new order, that is instantly added to the cached order list at the top
* Book list sort shouldn't matter because it is re retrieved if a new book is added. In case of update, it is added only if it is found
* in the cache list of books and replaced at that particular index. In case of removing a book, it is deleted if it is found.
* The page size must not be more than 10. Because orders are extracted with a limit of 10. Because relational object query in Backendless
* implements paging and the max is 10. You need to reconfigure the query and everything if page size is more than 10. However, for retrieving
* books, there shouldn't be any issue as the book objects are not related. But for consistency it is recommended that both be the same.
* */

package com.example.gronthomongol.backend;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.backend.models.Order;
import com.example.gronthomongol.ui.auth.AuthActivity;
import com.example.gronthomongol.ui.main.admin.AdminMainActivity;
import com.example.gronthomongol.ui.main.user.UserMainActivity;
import com.example.gronthomongol.ui.util.adapters.BooklistAdapterRV;
import com.example.gronthomongol.ui.util.adapters.BooklistAdapterRV_admin;
import com.example.gronthomongol.ui.util.listeners.EndlessScrollEventListener;
import com.example.gronthomongol.ui.util.adapters.OrderlistAdapterRV;
import com.example.gronthomongol.R;
import com.example.gronthomongol.ui.welcome.WelcomeScreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CONSTANTS {
    public static double CURRENT_APP_VERSION = 1.1;

    private static int MAX_NO_OF_BOOKS_PER_USER_PER_ORDER = 10;
    private static int MAX_NO_OF_ORDERS_PER_USER_PER_BOOK = 2;
    private static int MIN_NO_OF_ORDERS_PER_USER_PER_BOOK = 1;
    private static int ID_BOOKLIST = 17;
    private static int ID_BOOKLIST_ADMIN = 41;
    private static int ID_SPALSH_SCREEN = 29;
    private static int ID_LOGIN = 31;
    private static int ID_PLACE_ORDER = 19;
    private static int ID_VIEW_ORDERS = 37;
    private static int ID_VIEW_ORDERS_ADMIN = 43;
    private static int ID_PLACE_ORDER_ADD_MORE_BOOK = 23;
    private static int ID_BOOKLISTADMIN_BOOKDETAILS = 47;
    private static int ID_DELETE_BOOK_FROM_BOOK_DETAILS = 53;

    public static boolean sendNotificationToAdmins;
    public static boolean operational;
    public static boolean canOrder;
    public static String cannot_order_message;
    public static String non_operational_message;
    public static double min_version;
    public static int maximum_order_per_day;
    public static int currentOrderFilter = 0;       // none. index according to values/array_filter_by.xml
    public static OrderlistAdapterRV orderlistAdapterRV;

    public static int CURRENT_NUMBER_OF_ORDERS = 0;

    private static boolean showingDefaultBooklist = true;      // Not the search query list
    private static BackendlessUser currentUser;
    public static List<Book> bookListCached;
    public static List<Book> tempBookListCached = new ArrayList<>();
    public static List<Order> myOrdersCached;
    public static List<com.example.gronthomongol.backend.models.Request> userRequestsCached;
    public static ArrayList<Book> orderedBooks;
    private static int OFFSET = 0;      // Explicitly for booklist
    private static int MYORDEROFFSET = 0;
    private static int PAGE_SIZE = 13;
    private static int MY_ORDER_PAGE_SIZE = 13;     // MIGHT CAUSE TROUBLE. SET IT TO 10
    private static int REQUESTS_PAGE_SIZE = 13;
    private static int REQUESTS_OFFSET = 0;
    private  static DataQueryBuilder bookListQueryBuilder;
    private  static DataQueryBuilder userRequestsQueryBuilder;
    private  static DataQueryBuilder orderListQueryBuilder;
    private  static DataQueryBuilder SearchBookQueryBuilder;
    public static String NULLMARKER = "N/A_AUTO";

    public static int getOFFSET() {
        return OFFSET;
    }

    public static int getIdPlaceOrderAddMoreBook() {
        return ID_PLACE_ORDER_ADD_MORE_BOOK;
    }

    public static int getIdSpalshScreen() {
        return ID_SPALSH_SCREEN;
    }

    public static DataQueryBuilder getUserRequestsQueryBuilder() {
        return userRequestsQueryBuilder;
    }

    public static void setUserRequestsQueryBuilder(DataQueryBuilder userRequestsQueryBuilder) {
        CONSTANTS.userRequestsQueryBuilder = userRequestsQueryBuilder;
    }

    public static int getIdLogin() {
        return ID_LOGIN;
    }

    public static int getMyOrderPageSize() {
        return MY_ORDER_PAGE_SIZE;
    }

    public static List<Book> getTempBookListCached() {
        return tempBookListCached;
    }

    public static boolean isShowingDefaultBooklist() {
        return showingDefaultBooklist;
    }

    public static void setShowingDefaultBooklist(boolean showingDefaultBooklist) {
        CONSTANTS.showingDefaultBooklist = showingDefaultBooklist;
    }

    public static void setTempBookListCached(List<Book> tempBookListCached) {
        CONSTANTS.tempBookListCached = tempBookListCached;
    }

    public static int getRequestsPageSize() {
        return REQUESTS_PAGE_SIZE;
    }

    public static int getRequestsOffset() {
        return REQUESTS_OFFSET;
    }

    public static void setRequestsOffset(int requestsOffset) {
        REQUESTS_OFFSET = requestsOffset;
    }

    public static DataQueryBuilder getSearchBookQueryBuilder() {
        return SearchBookQueryBuilder;
    }

    public static void setSearchBookQueryBuilder(DataQueryBuilder searchBookQueryBuilder) {
        SearchBookQueryBuilder = searchBookQueryBuilder;
    }

    public static BackendlessUser getCurrentUser() {
        return currentUser;
    }

    public static int getIdViewOrders() {
        return ID_VIEW_ORDERS;
    }

    public static void setCurrentUser(BackendlessUser currentUser) {
        CONSTANTS.currentUser = currentUser;
    }

    public static int getIdBooklistadminBookdetails() {
        return ID_BOOKLISTADMIN_BOOKDETAILS;
    }

    public static int getMYORDEROFFSET() {
        return MYORDEROFFSET;
    }

    public static void setMYORDEROFFSET(int MYORDEROFFSET) {
        CONSTANTS.MYORDEROFFSET = MYORDEROFFSET;
    }

    public static DataQueryBuilder getOrderListQueryBuilder() {
        return orderListQueryBuilder;
    }

    public static void setOrderListQueryBuilder(DataQueryBuilder orderListQueryBuilder) {
        CONSTANTS.orderListQueryBuilder = orderListQueryBuilder;
    }

    public static int getMaxNoOfBooksPerUserPerOrder() {
        return MAX_NO_OF_BOOKS_PER_USER_PER_ORDER;
    }

    public static int getIdBooklistAdmin() {
        return ID_BOOKLIST_ADMIN;
    }

    public static int getIdDeleteBookFromBookDetails() {
        return ID_DELETE_BOOK_FROM_BOOK_DETAILS;
    }

    public static int getIdViewOrdersAdmin() {
        return ID_VIEW_ORDERS_ADMIN;
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }

    public static DataQueryBuilder getBookListQueryBuilder() {
        return bookListQueryBuilder;
    }

    public static int getIdBooklist() {
        return ID_BOOKLIST;
    }

    public static int getIdPlaceOrder() {
        return ID_PLACE_ORDER;
    }

    public static void setBookListQueryBuilder(DataQueryBuilder bookListQueryBuilder) {
        CONSTANTS.bookListQueryBuilder = bookListQueryBuilder;
    }

    public static List<Book> getBookListCached() {
        return bookListCached;
    }

    public static void setBookListCached(List<Book> bookListCached) {
        CONSTANTS.bookListCached = bookListCached;
    }

    public static List<Order> getMyOrdersCached() {
        return myOrdersCached;
    }

    public static void setMyOrdersCached(List<Order> myOrdersCached) {
        CONSTANTS.myOrdersCached = myOrdersCached;
    }

    public static int getMAX_NO_OF_ORDERS_PER_USER_PER_BOOK() {
        return MAX_NO_OF_ORDERS_PER_USER_PER_BOOK;
    }

    public static int getMIN_NO_OF_ORDERS_PER_USER_PER_BOOK() {
        return MIN_NO_OF_ORDERS_PER_USER_PER_BOOK;
    }

    public static void setOFFSET(int OFFSET) {
        CONSTANTS.OFFSET = OFFSET;
    }

    public static Comparator<Book> compareByName = new Comparator<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public static Comparator<Book> compareByWriter = new Comparator<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return o1.getWriter().compareTo(o2.getWriter());
        }
    };

    public static Comparator<Book> compareByQuantity = new Comparator<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return o1.getQuantity() - o2.getQuantity();
        }
    };

    public static void getConfigFile(final Context context, final int callingActivityId){
        String url = String.format("https://backendlessappcontent.com/%s/%s/files/config.json", CREDENTIALS.getApplicationId(),
                CREDENTIALS.getRestApiKey());

        RequestQueue requestQueue = Volley.newRequestQueue((Activity)context);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject config) {
                        try {
                            min_version = (double) config.get("min_version");
                            sendNotificationToAdmins = (boolean) config.get("sendNotificationToAdmins");
                            operational = (boolean) config.get("operational");
                            non_operational_message = (String) config.get("non_operational_message");
                            canOrder = (boolean) config.get("canOrder");
                            cannot_order_message = (String) config.get("cannot_order_message");
                            maximum_order_per_day = (int) config.get("maximum_order_per_day");

                            Log.i("file", min_version + "\t" + sendNotificationToAdmins + "\t" + operational + "\t" +
                                    non_operational_message + "\t" + canOrder + "\t" + cannot_order_message + "\t" + maximum_order_per_day);

                            if(CURRENT_APP_VERSION < min_version){
                                showErrorDialog((Activity)context, "Update Required", "Your app is outdated. Please update to continue", "okay",
                                        null, 19);
                            }

                            else if(!operational){
                                showErrorDialog((Activity)context, "Notice", non_operational_message, "Okay", null, 19);
                            }
                            else{
                                checkLoginStatus(context, callingActivityId);
                            }



                        } catch (JSONException e) {
                            showErrorDialog((Activity)context, "Error Occured", "Couldn't Load App Config. App must quit", "Okay", null,
                                    19);
                            Log.i("file", "onErrorResponse: Exception occurred reading the file" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        if(error.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageVolley))){
                            String title = "Connection Failed!";
                            String message = "Please Check Your Internet Connection";
                            showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                            Log.i("errorCode", "handleFault: Error Message = " + error.getMessage());
                        }
                        else{
                            showErrorDialog((Activity)context, "Error Occurred", "Something went wrong", "Quit", null, 19);
                        }
                        Log.i("file", "onErrorResponse: Error occurred getting the file " + error.getMessage());
                    }
                });

            requestQueue.add(jsonObjectRequest);

    }

    public static void checkLoginStatus(final Context context, final int callingActivityId){
        // #1
        // REQUIREMENT: ASSUMED COMING FROM THE SPLASH SCREEN ONLY
        // CHECK LOGIN

        final String currentUserId = Backendless.UserService.loggedInUser();

        // Get current user details from the database

        Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                // Get current number of orders from backendless
                AsyncCallback<Integer> callback = new AsyncCallback<Integer>()
                {
                    @Override
                    public void handleResponse( Integer value )
                    {
                        CONSTANTS.CURRENT_NUMBER_OF_ORDERS = value;
                        Log.i("counter", "handleResponse: Current number of orders: " + value);
                    }

                    @Override
                    public void handleFault( BackendlessFault backendlessFault )
                    {
                        Log.i("counter", "handleFault: " + backendlessFault.getMessage() + "\tcode: " + backendlessFault.getCode());
                        CONSTANTS.CURRENT_NUMBER_OF_ORDERS = 0;
                    }
                };

                Backendless.Counters.get( "current_number_of_orders", callback );

//                IAtomic<Integer> myCounter = Backendless.Counters.of( "current_number_of_orders", Integer.class );
//                CURRENT_NUMBER_OF_ORDERS = myCounter.get();
//                Log.i("counter", "handleResponse: Current number of orders: " + myCounter.get());

                // Note: Both of the below functions can be used to retrieve the current logged in user
                // But both of these functions might have been used somewhere in this project. So recommended that both be here.
                Backendless.UserService.setCurrentUser( response );
                CONSTANTS.setCurrentUser(response);

                // Retrieve Book List here
                // Book list retrieval requires to test if the current user is an admin. Thus it can't be placed anywhere earlier
                RetrieveBookListFromDatabaseInitially((Activity)context, callingActivityId, null);     // #2
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Couldn't get user details from the database. The program CANNOT continue
                // Getting user error
                String title;
                String message;

                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }
                else {
                    // Couldn't get user from database. Take the user to the login page after a logout attempt

                    Backendless.Messaging.unregisterDevice(new AsyncCallback<Integer>() {
                        @Override
                        public void handleResponse(Integer response) {
                            // Couldn't get user from database. Take the user to the login page after a logout attempt
                            Intent intent = new Intent((Activity)context, AuthActivity.class);
                            ((Activity)context).finish();
                            ((Activity)context).startActivity(intent);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // Couldn't get user from database. Take the user to the login page after a logout attempt
                            Intent intent = new Intent((Activity)context, AuthActivity.class);
                            ((Activity)context).finish();
                            ((Activity)context).startActivity(intent);
                        }
                    });
                }

            }
        });

    }

    public static void backendlessBookQuery(final Context context, final Dialog dialog, String whereClause, final BooklistAdapterRV_admin booklistAdapterRV_admin){
        final DataQueryBuilder searchBookQueryBuilder = DataQueryBuilder.create();
        searchBookQueryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Book.class).find(searchBookQueryBuilder, new AsyncCallback<List<Book>>() {
            @Override
            public void handleResponse(List<Book> response) {
                Log.i("search", "handleResponse: response size: " + response.size());
                CONSTANTS.setSearchBookQueryBuilder(searchBookQueryBuilder);
                dialog.dismiss();
                if(isShowingDefaultBooklist())
                    tempBookListCached.addAll(bookListCached);
                bookListCached.clear();
                bookListCached.addAll(response);
                setShowingDefaultBooklist(false);
                if(response.isEmpty()){
                    Toast.makeText(((Activity)context), "No result found!", Toast.LENGTH_LONG).show();
                }
                booklistAdapterRV_admin.notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                String title;
                String message;
                dialog.dismiss();

                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }
                else{
                    Toast.makeText(((Activity)context), "Sorry couldn't complete search query", Toast.LENGTH_SHORT).show();
                }

                Log.i("search", "handleFault: " + fault.getMessage());

            }
        });
    }

    // TODO REMEMBER TO PASTE UPDATES HERE TOO FROM THE ABOVE

    public static void backendlessBookQuery(final Context context, final Dialog dialog, String whereClause, final BooklistAdapterRV booklistAdapterRV){
        final DataQueryBuilder searchBookQueryBuilder = DataQueryBuilder.create();
        searchBookQueryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(Book.class).find(searchBookQueryBuilder, new AsyncCallback<List<Book>>() {
            @Override
            public void handleResponse(List<Book> response) {
                Log.i("search", "handleResponse: response size: " + response.size());
                CONSTANTS.setSearchBookQueryBuilder(searchBookQueryBuilder);
                dialog.dismiss();
                if(isShowingDefaultBooklist())
                    tempBookListCached.addAll(bookListCached); // Now the query list will be shown as the book list cached
                bookListCached.clear();
                bookListCached.addAll(response);
                setShowingDefaultBooklist(false);
                if(response.isEmpty()){
                    Toast.makeText(((Activity)context), "No result found!", Toast.LENGTH_LONG).show();
                }
                Log.i("search", String.format("booklistCached size now: %d\t tempBookListCached size: %d", bookListCached.size(), tempBookListCached.size()));

                booklistAdapterRV.notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                String title;
                String message;
                dialog.dismiss();

                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }
                else{
                    Toast.makeText(((Activity)context), "Sorry couldn't complete search query", Toast.LENGTH_SHORT).show();
                }

                Log.i("search", "handleFault: " + fault.getMessage());

            }
        });
    }

    public static void RetrieveBookListFromDatabaseInitially(final Context context, final int callingActivityId, final Dialog dialog){
        // For initial booklist database call only
        // REQUIREMENT: ONLY FOR THE INITIAL BOOKLIST RETRIEVAL WHERE NO CONTEXT, ADAPTER ETC ARE REQUIRED
        //              THEREFORE, COMING FROM EITHER SPLASH SCREEN OR LOGIN

        SharedPreferences pref = ((Activity)context).getSharedPreferences("preferences", 0); // 0 - for private mode

        setOFFSET(0);
        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        if(! (boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
            String whereClause = "quantity > 0";
            queryBuilder.setWhereClause(whereClause);
        }
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy(pref.getString("sortBy", "name"));
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of( Book.class ).find( queryBuilder, new AsyncCallback<List<Book>>() {
            @Override
            public void handleResponse( List<Book> response )
            {
                CONSTANTS.bookListCached = response;
                CONSTANTS.setBookListQueryBuilder(queryBuilder);    // To make this exact querybuilder accessible from all over the app
                Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                CONSTANTS.setOFFSET(CONSTANTS.getOFFSET()+CONSTANTS.getPageSize());

                // Retrieve Order List from Database
                boolean hasWhereClause = false;
                String whereClause = "";
                if(!(boolean) getCurrentUser().getProperty("admin")){
                    hasWhereClause = true;
                    whereClause = "user.email = '" + CONSTANTS.getCurrentUser().getEmail() + "'";
                }
                retrieveOrdersFromDatabase(context, hasWhereClause, whereClause, callingActivityId, dialog);
            }

            @Override
            public void handleFault( BackendlessFault fault )
            {
                // DONE
                if(dialog != null){
                    dialog.dismiss();
                }
                String title;
                String message;
                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    if(callingActivityId == getIdSpalshScreen()){
                        showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    }
                    else{
                        showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                    }

                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }

                else{
                    title = "Error Occurred";
                    message = "Couldn't Retrieve Book List from the Server";
                    if(callingActivityId == getIdSpalshScreen()){
                        showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    }
                    else{
                        showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                    }
                }
                Log.i("fault", fault.getMessage());
                //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void retrieveOrdersFromDatabase(final Context context, boolean hasWhereClause, String whereClause, final int callingActivityId, final Dialog dialog){
        // REQUIREMENT: SORTING SHOULDN'T BE CHANGED. IT IS ASSUMED TO BE CREATED DESC BY DEFAULT
        // No need to check if the user is an admin or not. It is handled before calling and this info is forwarded by the whereClause
        CONSTANTS.setMYORDEROFFSET(0);

        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        if(hasWhereClause) {
            queryBuilder.setWhereClause(whereClause);
        }
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setPageSize( CONSTANTS.getMyOrderPageSize() ).setOffset( CONSTANTS.getMYORDEROFFSET() );
        Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> response) {
                //Log.i("myOrders_retrieve", "SplashScreen/handleResponse: where Clause: " + whereClause);
                Log.i("myOrders_retrieve", "SplashScreen/handleResponse: My orders retrieved. response size = " + response.size());
                CONSTANTS.setMyOrdersCached(response);
                CONSTANTS.setOrderListQueryBuilder(queryBuilder);
                CONSTANTS.setMYORDEROFFSET(CONSTANTS.getMYORDEROFFSET() + CONSTANTS.getMyOrderPageSize());


                if(callingActivityId == getIdLogin() || callingActivityId == getIdSpalshScreen()){
                    if(dialog != null){
                        dialog.dismiss();
                    }
                    if(!(boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
                        Intent intent = new Intent((Activity) context, UserMainActivity.class);
                        intent.putExtra(((Activity) context).getString(R.string.activityIDName), CONSTANTS.getIdSpalshScreen());
                        ((Activity) context).startActivity(intent);
                        ((Activity) context).finish();
                    }
                    else{
                        Intent intent = new Intent((Activity) context, AdminMainActivity.class);
                        intent.putExtra(((Activity) context).getString(R.string.activityIDName), CONSTANTS.getIdSpalshScreen());
                        ((Activity) context).startActivity(intent);
                        ((Activity) context).finish();
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                if(dialog != null){
                    dialog.dismiss();
                }
                String title;
                String message;
                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    if(callingActivityId == getIdSpalshScreen()){
                        showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    }
                    else{
                        showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                    }

                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }

                else{
                    title = "Error Occurred";
                    message = "Couldn't Retrieve Book List from the Server";
                    if(callingActivityId == getIdSpalshScreen()){
                        showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    }
                    else{
                        showErrorDialog((Activity)context, title, message, "Okay", null, 0);
                    }
                }
                Log.i("fault", fault.getMessage());
                //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void freshRetrieveFromDatabase(final Context context, final BooklistAdapterRV booklistAdapterRV, String sortBy, final Dialog dialog,
                                                 final RecyclerView recyclerView, final EndlessScrollEventListener endlessScrollEventListener){

//      REQUIREMENTS: MUST BE CALLED AFTER IT IS DETERMINED IF THE USER IS AN ADMIN OR NOT
//                    MUST BE CALLED AFTER THE FULL LIST DISPLAY REQUIREMENTS ARE INITIALIZED
//                    CANNOT BE USED AS THE INITIAL DATABASE CALL. INITIAL CALL IS DEFINED IN THE SplashScreen ACTIVITY

        // Internal Changes
        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: before sortBy = " + sortBy);

        final int oldOffset = CONSTANTS.getOFFSET();
        CONSTANTS.setOFFSET(0);
        SharedPreferences pref = context.getSharedPreferences("preferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("sortBy", sortBy);
        editor.commit();

        Log.i("shared_preference", "freshRetrieveFromDatabase: sortBy = " + sortBy);
        Log.i("shared_preference", "freshRetrieveFromDatabase: value from sharedPreference: " + pref.getString("sortBy", "default"));


        // Database Stuff
        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        // Include where clause only if the user is not an admin
        if(! (boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
            String whereClause = "quantity > 0";
            queryBuilder.setWhereClause(whereClause);
        }
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy(sortBy);
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of( Book.class ).find( queryBuilder,
                new AsyncCallback<List<Book>>()
                {
                    @Override
                    public void handleResponse( List<Book> response )
                    {
//                        booklistAdapter = new BooklistAdapter(getApplicationContext(), response);
//                        listView.setAdapter(booklistAdapter);

                        CONSTANTS.bookListCached.clear();
                        CONSTANTS.bookListCached.addAll(response);

                        Log.i("booklist_retrieve", "received list[0] = " + response.get(0).getName());
                        Log.i("booklist_retrieve", "cached list[0] = " + CONSTANTS.bookListCached.get(0).getName());

                        endlessScrollEventListener.reset();
                        booklistAdapterRV.notifyDataSetChanged();
                        CONSTANTS.setBookListQueryBuilder(queryBuilder);    // To make this exact querybuilder accessible from all over the app
                        Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                        CONSTANTS.setOFFSET(CONSTANTS.getOFFSET()+CONSTANTS.getPageSize());

                        dialog.dismiss();

                    }

                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        dialog.dismiss();
                        CONSTANTS.setOFFSET(oldOffset);
                        String title;
                        String message;

                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                            Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                        }
                        else{
                            Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }

                        Log.e("fault", fault.getMessage());

                        //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    public static void freshRetrieveFromDatabase(final Context context, final BooklistAdapterRV_admin booklistAdapterRV_admin, String sortBy, final Dialog dialog,
                                                 final RecyclerView recyclerView, final EndlessScrollEventListener endlessScrollEventListener){

//      REQUIREMENTS: MUST BE CALLED AFTER IT IS DETERMINED IF THE USER IS AN ADMIN OR NOT
//                    MUST BE CALLED AFTER THE FULL LIST DISPLAY REQUIREMENTS ARE INITIALIZED
//                    CANNOT BE USED AS THE INITIAL DATABASE CALL. INITIAL CALL IS DEFINED IN THE SplashScreen ACTIVITY

        // Internal Changes
        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: before sortBy = " + sortBy);
        final int oldOffset = CONSTANTS.getOFFSET();

        CONSTANTS.setOFFSET(0);
        SharedPreferences pref = context.getSharedPreferences("preferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("sortBy", sortBy);
        editor.apply();


        Log.i("shared_preference", "freshRetrieveFromDatabase: sortBy = " + sortBy);
        Log.i("shared_preference", "freshRetrieveFromDatabase: value from sharedPreference: " + pref.getString("sortBy", "default"));

        // Database Stuff
        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        // Include where clause only if the user is not an admin
        if(! (boolean) CONSTANTS.getCurrentUser().getProperty("admin")) {
            String whereClause = "quantity > 0";
            queryBuilder.setWhereClause(whereClause);
        }
        queryBuilder.addAllProperties();
        queryBuilder.setSortBy(sortBy);
        queryBuilder.setPageSize( CONSTANTS.getPageSize() ).setOffset( CONSTANTS.getOFFSET() );
        Backendless.Data.of( Book.class ).find( queryBuilder, new AsyncCallback<List<Book>>()
                {
                    @Override
                    public void handleResponse( List<Book> response )
                    {
//                        booklistAdapter = new BooklistAdapter(getApplicationContext(), response);
//                        listView.setAdapter(booklistAdapter);

                        CONSTANTS.bookListCached.clear();
                        CONSTANTS.bookListCached.addAll(response);

                        Log.i("booklist_retrieve", "received list[0] = " + response.get(0).getName());
                        Log.i("booklist_retrieve", "cached list[0] = " + CONSTANTS.bookListCached.get(0).getName());

                        endlessScrollEventListener.reset();
                        booklistAdapterRV_admin.notifyDataSetChanged();
                        CONSTANTS.setBookListQueryBuilder(queryBuilder);    // To make this exact querybuilder accessible from all over the app
                        Log.i("booklist_retrieve", "Booklist retrieved. Size = " + response.size());
                        CONSTANTS.setOFFSET(CONSTANTS.getOFFSET()+CONSTANTS.getPageSize());

                        dialog.dismiss();

                    }

                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        dialog.dismiss();
                        String title;
                        String message;

                        CONSTANTS.setOFFSET(oldOffset);

                        if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                            title = "Connection Failed!";
                            message = "Please Check Your Internet Connection";
                            showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                            Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                        }
                        else{
                            Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }

                        Log.e("fault", fault.getMessage());

                        //Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    public static void freshOrderRetrieveFromDatabase(final Context context, final OrderlistAdapterRV orderlistAdapterRV, String whereClause, final Dialog dialog,
                                                      final EndlessScrollEventListener endlessScrollEventListener, final int which){
        final int oldOffset = CONSTANTS.getMYORDEROFFSET();
        CONSTANTS.setMYORDEROFFSET(0);

        final DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setPageSize(CONSTANTS.getMyOrderPageSize()).setOffset(CONSTANTS.getMYORDEROFFSET());
        if(!whereClause.equals(NULLMARKER))
            dataQueryBuilder.setWhereClause(whereClause);
        dataQueryBuilder.addAllProperties();
        dataQueryBuilder.setSortBy("created DESC");

        Backendless.Data.of(Order.class).find(dataQueryBuilder, new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> response) {
                CONSTANTS.myOrdersCached.clear();
                CONSTANTS.myOrdersCached.addAll(response);

                Log.i("orderlist_retrieve", "received list[0] = " + response.get(0).getRecipient_Name());

                endlessScrollEventListener.reset();
                orderlistAdapterRV.notifyDataSetChanged();
                CONSTANTS.setOrderListQueryBuilder(dataQueryBuilder);    // To make this exact querybuilder accessible from all over the app
                Log.i("orderlist_retrieve", "Booklist retrieved. Size = " + response.size());
                CONSTANTS.setMYORDEROFFSET(CONSTANTS.getMYORDEROFFSET()+CONSTANTS.getMyOrderPageSize());

                CONSTANTS.currentOrderFilter = which;
                dialog.dismiss();
            }

            @Override
            public void handleFault(BackendlessFault fault) {

                dialog.dismiss();
                String title;
                String message;

                CONSTANTS.setMYORDEROFFSET(oldOffset);

                if( fault.getMessage().equals(((Activity)context).getString(R.string.connectionErrorMessageBackendless) )) {
                    title = "Connection Failed!";
                    message = "Please Check Your Internet Connection";
                    showErrorDialog_splashScreen((Activity)context, title, message, "Retry", "Quit");
                    Log.i("errorCode", "handleFault: error Code: " + fault.getCode() + "\t Error Message = " + fault.getMessage());
                }
                else{
                    Toast.makeText((Activity)context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                Log.e("fault", fault.getMessage());
            }
        });
    }

    public static void sendNotificationToTheAdmins(String message, String messageTitle) {
        if(CONSTANTS.sendNotificationToAdmins){
            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "You just got a private push notification!");
            publishOptions.putHeader("android-content-title", messageTitle);
            publishOptions.putHeader("android-content-text", "Push Notifications Text");
            Backendless.Messaging.publish("admin", message, publishOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus response) {
                    Log.i("notification", "handleResponse: Notification sent to the admins");
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Log.i("notification", "handleFault: Notification sending to the admins failed\t" + fault.getMessage());
                }
            });
        }
    }

    public static void showErrorDialog_splashScreen(final Context context, String title, String message, String positiveButton, String negativeButton)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity)context));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(positiveButton,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        arg0.dismiss();
                        Intent intent = new Intent(((Activity)context), WelcomeScreenActivity.class);
                        ((Activity)context).startActivity(intent);
                        ((Activity)context).finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity)context).finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void showErrorDialog(final Context context, String title, String message, String positiveButton, String negativeButton, final int fromOperationId){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity)context));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(positiveButton,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        arg0.dismiss();
                        if(fromOperationId == 19) {   // Close current activity
                            ((Activity)context).finish();
                        }
                    }
                });
        if(negativeButton != null){
            alertDialogBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
