/*  Orderlist Sort must be by last created. Because after creating a new order, that is instantly added to the cached order list at the top
* Book list sort shouldn't matter because it is re retrieved if a new book is added. In case of update, it is added only if it is found
* in the cache list of books and replaced at that particular index. In case of removing a book, it is deleted if it is found.
* The page size must not be more than 10. Because orders are extracted with a limit of 10. Because relational object query in Backendless
* implements paging and the max is 10. You need to reconfigure the query and everything if page size is more than 10. However, for retrieving
* books, there shouldn't be any issue as the book objects are not related. But for consistency it is recommended that both be the same.
* */

package com.example.gronthomongol;

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

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserTokenStorageFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class CONSTANTS {
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
    private static boolean showingDefaultBooklist = true;      // Not the search query list
    private static BackendlessUser currentUser;
    public static List<Book> bookListCached;
    public static List<Book> tempBookListCached = new ArrayList<>();
    public static List<Order> myOrdersCached;
    public static ArrayList<Book> orderedBooks;
    private static int OFFSET = 0;      // Explicitly for booklist
    private static int MYORDEROFFSET = 0;
    private static int PAGE_SIZE = 13;
    private static int MY_ORDER_PAGE_SIZE = 13;
    private  static DataQueryBuilder bookListQueryBuilder;
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

    public static void checkLoginStatus(final Context context, final int callingActivityId){
        // #1
        // REQUIREMENT: ASSUMED COMING FROM THE SPLASH SCREEN ONLY
        // CHECK LOGIN

        final String currentUserId = Backendless.UserService.loggedInUser();

        // Get current user details from the database

        Backendless.UserService.findById(currentUserId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
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
                    Intent intent = new Intent((Activity)context, login.class);
                    ((Activity)context).finish();
                    ((Activity)context).startActivity(intent);
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
                        Intent intent = new Intent((Activity) context, com.example.gronthomongol.booklist.class);
                        intent.putExtra(((Activity) context).getString(R.string.activityIDName), CONSTANTS.getIdSpalshScreen());
                        ((Activity) context).startActivity(intent);
                        ((Activity) context).finish();
                    }
                    else{
                        Intent intent = new Intent((Activity) context, com.example.gronthomongol.booklist_admin.class);
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

        CONSTANTS.setOFFSET(0);
        SharedPreferences pref = context.getSharedPreferences("preferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("sortBy", sortBy);
        editor.commit();


        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: sortBy = " + sortBy);

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

        CONSTANTS.setOFFSET(0);
        SharedPreferences pref = context.getSharedPreferences("preferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("sortBy", sortBy);
        editor.commit();


        Log.i("booklist_retrieve", "freshRetrieveFromDatabase: sortBy = " + sortBy);

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

    public static void sendNotificationToTheAdmins(String message, String messageTitle) {
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.putHeader( "android-ticker-text", "You just got a private push notification!" );
        publishOptions.putHeader( "android-content-title", messageTitle );
        publishOptions.putHeader( "android-content-text", "Push Notifications Text" );
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
                        Intent intent = new Intent(((Activity)context), SplashScreen.class);
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
