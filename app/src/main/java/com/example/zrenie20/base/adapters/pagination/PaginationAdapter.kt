package com.example.zrenie20.base.adapters.pagination

import android.content.Context
import androidx.annotation.StringDef
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat

import com.example.zrenie20.R
import com.example.zrenie20.base.adapters.AdapterDelegate
import com.example.zrenie20.base.adapters.DelegationAdapter

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


class PaginationAdapter<T: Any>(
    private val loader: Loader?,
    @Direction direction: String = Direction.TO_END,
    itemThreshold: Int = LoadDetector.DEFAULT_ITEM_THRESHOLD
) : DelegationAdapter<T>(false) {
    internal var paginationLoaderText: String? = ""
    private var retryCallback: (() -> Unit)? = null
    private var isErrorShows = false
    private val loadDetector: LoadDetector
    private var isLastPage: Boolean = false

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(Direction.TO_END, Direction.TO_START)
    annotation class Direction {
        companion object {
            const val TO_START = "start"
            const val TO_END = "end"
        }
    }

    interface Loader {
        fun onLoadMore(offset: Int)
    }

    constructor(loader: Loader, isSticky: Boolean?) : this(
        loader,
        Direction.TO_END,
        LoadDetector.DEFAULT_ITEM_THRESHOLD
    )

    init {
        if (direction == Direction.TO_END) {
            this.loadDetector = LoadFromEndDetector(itemThreshold)
        } else {
            this.loadDetector = LoadFromStartDetector(itemThreshold)
        }
    }

    open fun setProgressText(mPaginationLoaderText: String?) {
        paginationLoaderText = mPaginationLoaderText
    }

    /**
     * Изменение состояния блока ошибки
     * @param isNeedToShowError необходимость показа блока ошибки (в противном случае - блок загрузки)
     */
    fun showErrorOrLoadingBlock(isNeedToShowError: Boolean) {
        isErrorShows = isNeedToShowError
        notifyItemChanged(itemCount - 1)
    }

    /**
     * Присвоение коллбэка повторной попытки
     * @param newRetryCallback коллбэк для выполнения повторной попытки
     */
    fun setRetryCallback(newRetryCallback: () -> Unit) {
        retryCallback = newRetryCallback
    }

    /**
     * Присвоение нового статуса для детектора
     * @param newState новый статус
     */
    fun setLoadDetectorState(newState: Boolean) {
        loadDetector.setLoadingState(newState)
    }

    /**
     * Получение статуса для детектора
     * @return текущий статус
     */
    fun getLoadDetectorState(): Boolean = loadDetector.isLoading

    /**
     * Метод для смены порядка делегатов индикатора загрузки и пустого делегата
     * @param emptyDelegate пустой делегат
     */
    fun reorderEmptyAndProgressBarDelegates(emptyDelegate: AdapterDelegate<T>){
        manager.removeDelegate(emptyDelegate)
        manager.addDelegate(emptyDelegate)
    }

    open override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        loadDetector.onAttachedToRecyclerView(recyclerView, this as PaginationAdapter<Any>)
    }

    open override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        loadDetector.onDetachedFromRecyclerView(recyclerView)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    internal open fun loadItems(itemsCount: Int) {
        //Log.d(TAG, "loadItems");
        if (loader != null && !isLastPage) {
            //Log.d(TAG, "loadItems" + true);
            loadDetector.setLoadingState(true)
            loader.onLoadMore(itemsCount)
        }
    }

    open override fun addAll(items: List<T>?, startPosition: Int) {
        var startPosition = startPosition
        val fromLoader = loadDetector.isLoading || itemCount == 0
        if (fromLoader) {
            isLastPage = items!!.isEmpty()
            if (itemCount != 0) {
                loadDetector.setLoadingState(false)
                // TODO: DO 22.09.2016 fix this and loadUP
                startPosition--
                if (startPosition < 0) {
                    startPosition = 0
                }
            }
        }

        super.addAll(items, startPosition)
        /*		if (fromLoader && !isLastPage && loadDetector.isItemsNotFitScreen(items.size())) {
			loadItems(getItemCount());
		}*/
    }

    open fun loadNew(items: List<T>, startPosition: Int) {
        if (itemCount == 0) {
            addAll(items, startPosition)
        } else {
            loadDetector.setLoadingState(false)
            for (i in items.indices.reversed()) {
                add(items[i], 0)
            }
        }
    }

    open override fun clear() {
        super.clear()
        isLastPage = false
    }

    /*
     *need to enable pagination
     * */
    open fun resetIsLastPage() {
        isLastPage = false
    }

    open fun disablePagination() {
        isLastPage = true
    }
}
