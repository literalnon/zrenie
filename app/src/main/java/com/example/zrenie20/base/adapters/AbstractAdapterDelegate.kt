package com.example.zrenie20.base.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

abstract class AbstractAdapterDelegate<I : T, T, VH : RecyclerView.ViewHolder> : AdapterDelegate<T>() {

    override fun isForViewType(items: List<T>, position: Int): Boolean {
        val item = items.getOrNull(position)

        return if (item != null) {
            isForViewType(item, items, position)
        } else {
            false
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, items: List<T>, position: Int) {
        onBindViewHolder(holder as VH, items[position] as I)
        // May be replace to it
        onBindViewHolder(holder, items[position] as I, items, position)
    }

    /**
     * Called to determine whether this AdapterDelegate is the responsible for the given item in the list or not
     * element
     *
     * @param item     The item from the list at the given position
     * @param items    The items from adapters dataset
     * @param position The items position in the dataset (list)
     * @return true if this AdapterDelegate is responsible for that, otherwise false
     */
    protected abstract fun isForViewType(item: T, items: List<T>, position: Int): Boolean

    /**
     * Creates the  [RecyclerView.ViewHolder] for the given data source item
     *
     * @param parent The ViewGroup parent of the given datasource
     * @return ViewHolder
     */
    abstract override fun onCreateViewHolder(parent: ViewGroup): VH

    /**
     * Called to bind the [RecyclerView.ViewHolder] to the item of the dataset
     *
     * @param holder The ViewHolder
     * @param item   The data item
     */
    protected open fun onBindViewHolder(holder: VH, item: I) {

    }

    protected open fun onBindViewHolder(holder: VH, item: I, items: List<T>, position: Int) {

    }
}
