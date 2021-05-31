package com.example.zrenie20.base.adapters.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

internal open class LoadFromEndDetector(itemThreshold: Int) : LoadDetector(itemThreshold) {

    private val TAG = "LoadFromEndDetector"

    override fun enableProgressItem(isEnable: Boolean) {
        if (isEnable) {
            //adapter?.add(progressItem)
        } else {
            adapter?.let {
                it.remove(it.itemCount - 1)
            }
        }
    }

    override fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    //scroll up, all visible items already loaded
                    return
                }

                val loadedItemsCount = adapter!!.itemCount
                val lastVisibleItem = findLastVisibleItemPosition(layoutManager)

                //Log.d(TAG, "scroll " + loadedItemsCount + " : " + lastVisibleItem + " : " +isLoading() + " : " + getItemThreshold());
                if (!isLoading && loadedItemsCount <= lastVisibleItem + itemThreshold) {
                    //Log.d(TAG, "scroll true" + loadedItemsCount);
                    adapter!!.loadItems(loadedItemsCount)
                }
            }
        }
    }

    // TODO: 11.09.15 refactor this
    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is LinearLayoutManager) {
            return layoutManager.findLastVisibleItemPosition()
        } else {
            val pos = (layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
            // TODO: 09.03.16 refactor this to find max
            return pos[pos.size - 1]
        }
    }
}

internal class NestedLoadDetector(itemThreshold: Int) : LoadFromEndDetector(itemThreshold) {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView, paginationAdapter: PaginationAdapter<Any>) {
        this.layoutManager = recyclerView.layoutManager
        // TODO: 09.03.16 maybe create LoadManager - and pass into Loader and LoadDetector
        this.adapter = paginationAdapter
    }

    /*public getScrollListener() {
		return new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy <= 0) {
					//scroll up, all visible items already loaded
					return;
				}

				final int loadedItemsCount = adapter.getItemCount();
				final int lastVisibleItem = findLastVisibleItemPosition(layoutManager);

				Log.d(TAG, "scroll " + loadedItemsCount + " : " + lastVisibleItem + " : " +isLoading() + " : " + getItemThreshold());
				if (!isLoading() && loadedItemsCount <= (lastVisibleItem + getItemThreshold())) {
					Log.d(TAG, "scroll true" + loadedItemsCount);
					adapter.loadItems(loadedItemsCount);
				}
			}
		};
	}*/

    // TODO: 11.09.15 refactor this
    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager): Int {
        if (layoutManager is LinearLayoutManager) {
            return layoutManager.findLastVisibleItemPosition()
        } else {
            val pos = (layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
            // TODO: 09.03.16 refactor this to find max
            return pos[pos.size - 1]
        }
    }
}