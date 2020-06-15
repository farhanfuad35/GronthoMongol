package com.example.gronthomongol.ui.util.listeners;

// Source: https://baraabytes.com/android-endless-scrolling-with-recyclerview/


import android.util.Log;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public abstract class EndlessScrollEventListener extends RecyclerView.OnScrollListener {

    private LinearLayoutManager mLinearLayoutManager;


    /** is number of items that we could have after our
     *  current scroll position before we start loading
     more items */
    private int visibleThreshold = 3;
    /** to keep track of the page that we would like to
     * retrieve from a server our database
     * */
    private int currentPage = 0;
    /** total number of items that we retrieve lastly*/
    private int previousTotalItemCount = 0;
    /** indicating whether we are loading new dataset or not*/
    private boolean loading = true;
    /** the initial index of the page that'll start from */
    private int startingPageIndex = 0;

    /******* variables we could get from linearLayoutManager *******/

    /** the total number of items that we currently have on our recyclerview and we
     * get it from linearLayoutManager */
    private int totalItemCount;

    /** the position of last visible item in our view currently
     * get it from linearLayoutManager */
    private int lastVisibleItemPosition;

    public EndlessScrollEventListener(LinearLayoutManager linearLayoutManager) {
        mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        totalItemCount = mLinearLayoutManager.getItemCount();
        lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();

        Log.i("EndlessScroll", String.format("totalItemCount = '%d'\tpreviousTotalItemCount = '%d'\tcurrentPage = '%d'\tloading = " + loading + "\t" +
                "Starting Page = '%d'", totalItemCount, previousTotalItemCount, currentPage, startingPageIndex));

        // first case
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) { this.loading = true; }

            Log.i("EndlessScroll", "onScrolled firstCase");
        }

        // second case
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
            Log.i("EndlessScroll", "onScrolled secongCase");
        }

        // third case
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            currentPage++;
            Log.i("EndlessScroll", "onScrolled thirdCase");
            onLoadMore(currentPage, recyclerView);
            loading = true;
        }


    }

    // should be called if we do filter(search) to our list
    public void reset(){
        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
    }

    // Define the place where we load the dataset
    public abstract void onLoadMore(int pageNum, RecyclerView recyclerView);

}