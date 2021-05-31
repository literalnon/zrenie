package com.example.zrenie20.base.adapters

import android.util.Log
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterManager<T> internal constructor() {
    private val delegates = SparseArrayCompat<AdapterDelegate<T>>()

    fun addDelegate(
            delegate: AdapterDelegate<T>,
            position: Int? = null
    ): AdapterManager<T> {
        val viewType = delegates.size()
        return addDelegate(
                viewType = viewType,
                delegate = delegate,
                position = position
        )
    }

    @JvmOverloads
    fun addDelegate(viewType: Int,
                    delegate: AdapterDelegate<T>,
                    allowReplacing: Boolean = false,
                    position: Int? = null
    ): AdapterManager<T> {
        require(!(!allowReplacing && delegates.get(viewType) != null)) {
            ("An AdapterDelegate is already registered for the viewType = "
                    + viewType
                    + ". Already registered AdapterDelegate is "
                    + delegates.get(viewType))
        }

        if (position != null) {
            delegates.put(position, delegate)
        } else {
            delegates.put(viewType, delegate)
        }

        return this
    }

    fun removeDelegate(viewType: Int): AdapterManager<T> {
        delegates.remove(viewType)
        return this
    }

    fun clearDelegates(): AdapterManager<T> {
        delegates.clear()
        return this
    }

    fun removeDelegate(delegate: AdapterDelegate<T>): AdapterManager<T> {
        val index = delegates.indexOfValue(delegate)
        if (index >= 0) {
            delegates.removeAt(index)
        }
        return this
    }

    fun getItemViewType(items: List<T>, position: Int): Int {
        var i = 0
        val size = delegates.size()
        while (i < size) {
            val adapter = delegates.valueAt(i)

            if (adapter.isForViewType(items, position)) {
                return delegates.keyAt(i)
            }
            i++
        }

        return size

        // TODO: 23.08.2019 Нам это точно необходимо? По факту для избежания ошибки используется заглушка
        // TODO: в виде пустого делегата. Возможно следует сократить или добавить логгер
        /*throw NullPointerException("No AdapterDelegate added that matches position=" + position
                + " in data source" + " type: " + items[position].toString() + dopInfo)*/

    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterDelegate = delegates.get(viewType)
                ?: throw NullPointerException("No AdapterDelegate added for ViewType $viewType")
        return adapterDelegate.onCreateViewHolder(parent)
                ?: throw NullPointerException("ViewHolder returned from AdapterDelegate "
                        + adapterDelegate
                        + " for ViewType ="
                        + viewType
                        + " is null!")
    }

    fun onBindViewHolder(items: List<T>, position: Int, holder: RecyclerView.ViewHolder) {
        val adapterDelegate = delegates.get(holder.itemViewType)
                ?: throw NullPointerException("No AdapterDelegate found for item at position = "
                        + position
                        + " for viewType = "
                        + holder.itemViewType)
        adapterDelegate.onBindViewHolder(holder, items, position)
    }

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>, item: T?) {
        val adapterDelegate = delegates.get(holder.itemViewType)
                ?: throw NullPointerException("No AdapterDelegate found for item at position = "
                        + position
                        + " for viewType = "
                        + holder.itemViewType)
        adapterDelegate.onBindViewHolder(holder, position, payloads, item)
    }

    fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        val adapterDelegate = delegates.get(holder.itemViewType)
                ?: throw NullPointerException("No AdapterDelegate found "
                        + " for viewType = "
                        + holder.itemViewType)
        adapterDelegate.onViewRecycled(holder)
    }
}
