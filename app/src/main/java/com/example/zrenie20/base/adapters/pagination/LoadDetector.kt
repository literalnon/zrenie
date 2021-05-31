package com.example.zrenie20.base.adapters.pagination

import androidx.recyclerview.widget.RecyclerView

internal abstract class LoadDetector(val itemThreshold: Int) {

    @Volatile
    var isLoading: Boolean = false
        private set

    protected var layoutManager: RecyclerView.LayoutManager? = null
    protected var adapter: PaginationAdapter<Any>? = null
    private var scrollListener: RecyclerView.OnScrollListener? = null

    internal open fun onAttachedToRecyclerView(recyclerView: RecyclerView, paginationAdapter: PaginationAdapter<Any>) {
        this.layoutManager = recyclerView.layoutManager
        // TODO: 09.03.16 maybe create LoadManager - and pass into Loader and LoadDetector
        this.adapter = paginationAdapter
        scrollListener = getScrollListener()
        recyclerView.addOnScrollListener(scrollListener!!)
    }

    fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnScrollListener(scrollListener!!)
        this.scrollListener = null
        this.layoutManager = null
        this.adapter = null
    }

    abstract fun enableProgressItem(isEnable: Boolean)

    abstract fun getScrollListener(): RecyclerView.OnScrollListener

    fun setLoadingState(isLoading: Boolean) {
        //Log.d("TAG", "isLoading " + isLoading);
        this.isLoading = isLoading
        enableProgressItem(isLoading)
    }

    /**
     * Check if items fit screen
     *
     * @param loadedItemsCount loaded items count
     * @return <tt>true</tt>, if items don't fit screen and need downloading, <tt>false</tt> - otherwise
     */
    fun isItemsNotFitScreen(loadedItemsCount: Int): Boolean {
        val childCount = layoutManager!!.childCount
        return childCount == 0 || adapter!!.itemCount - loadedItemsCount <= childCount
    }

    companion object {
        val DEFAULT_ITEM_THRESHOLD = 5
    }
}
