package com.example.zrenie20.base.adapters

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup

import com.example.zrenie20.R

import java.util.ArrayList
import java.util.HashMap

open class DelegationAdapter<T> : BaseAdapter<T, RecyclerView.ViewHolder> {
    val manager = AdapterManager<T>()
    protected var headerPositions: HashMap<Int, Pair<Int, StickDelegate>> = HashMap()
    protected var isSticky: Boolean = false

    constructor() {
        this.isSticky = false
    }

    constructor(isSticky: Boolean) {
        this.isSticky = isSticky
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return manager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        Log.e("BETCITY_DELEG_ADAPTER", "onBindViewHolder position : ${position}, itemCount : ${itemCount}")
        if (position < itemCount) {
            manager.onBindViewHolder(items, position, holder)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
//        Log.e("BETCITY_DELEG_ADAPTER", "onBindViewHolder payloads : ${payloads.count()}, position : ${position}, itemCount : ${itemCount}")
        if (payloads.count() == 0) {
            manager.onBindViewHolder(items, position, holder)
        } else if (position < itemCount) {
            manager.onBindViewHolder(holder, position, payloads, items.getOrNull(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return manager.getItemViewType(items, position)
    }

    override fun replaceAll(items: List<T>) {
        if (isSticky) {
            val newItems = ArrayList<T>()
            headerPositions = HashMap()

            var lastCreateHeaderPosition = 0
            var headersCount = 0

            for (i in items.indices) {
                if (items[i] is StickDelegate) {
                    if (headerPositions[lastCreateHeaderPosition] != null &&
                            TextUtils.equals(headerPositions[lastCreateHeaderPosition]!!.second.stickId, (items[i] as StickDelegate).stickId)
                    ) {
                        headerPositions[newItems.size] = headerPositions[lastCreateHeaderPosition]!!
                    } else {
                        headerPositions[i - headersCount] = Pair(headersCount, items[i] as StickDelegate)
                        lastCreateHeaderPosition = i - headersCount
                    }

                    headersCount++
                } else {
                    newItems.add(items[i])
                    if (headerPositions[lastCreateHeaderPosition] != null) {
                        headerPositions[newItems.size] = headerPositions[lastCreateHeaderPosition]!!
                    }
                }
            }
            super.replaceAll(newItems)
        } else {
            super.replaceAll(items)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        manager.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }
}