package com.example.zrenie20.base.adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

abstract class AdapterDelegate<T> {

    /**
     * Called to determine whether this AdapterDelegate is the responsible for the given data
     * element.
     *
     * @param items    The data source of the Adapter
     * @param position The position in the datasource
     * @return true, if this item is responsible, otherwise false
     */
    abstract fun isForViewType(items: List<T>, position: Int): Boolean

    /**
     * Creates the [RecyclerView.ViewHolder] for the given data source item
     *
     * @param parent The ViewGroup parent of the given datasource
     * @return The new instantiated [RecyclerView.ViewHolder]
     */
    abstract fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    /**
     * Called to bind the [RecyclerView.ViewHolder] to the item of the datas source set
     *
     * @param holder   The [RecyclerView.ViewHolder] to bind
     * @param items    The data source
     * @param position The position in the datasource
     */
    abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, items: List<T>, position: Int)

    /**
     * Called when view detached from recycle
     *
     * @param holder   The [RecyclerView.ViewHolder] to bind
     */
    open fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        //Log.e("delegate", "onViewRecycled")
    }


    /**
     * Called to bind the [RecyclerView.ViewHolder] to the item of the datas source set
     *
     * @param holder   The [RecyclerView.ViewHolder] to bind
     * @param items    The data source
     * @param position The position in the datasource
     * @param payloads The diff in the datasource
     */
    open fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>, item: T?) {}
}