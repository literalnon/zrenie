package com.example.zrenie20.base.adapters.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

internal class LoadFromStartDetector(itemThreshold: Int) : LoadDetector(itemThreshold) {

    override fun enableProgressItem(isEnable: Boolean) {
        if (isEnable) {
            //adapter!!.add(progressItem, HEADER_POSITION)
        } else {
            adapter!!.remove(HEADER_POSITION)
        }
    }

    override fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy >= 0) {
                    //scroll down, all visible items already loaded
                    return
                }

                val firstVisibleItem = findFirstVisibleItemPosition(layoutManager)
                if (!isLoading && firstVisibleItem <= itemThreshold) {
                    adapter!!.loadItems(adapter!!.itemCount)
                }
            }
        }
    }

    private fun findFirstVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is LinearLayoutManager) {
            return layoutManager.findFirstVisibleItemPosition()
        } else {
            val pos = (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)
            // TODO: 09.03.16 refactor this to find min
            return pos[0]
        }
    }

    companion object {
        private val HEADER_POSITION = 0

        private val TAG = "LoadFromStartDetector"
    }
}
