package com.example.zrenie20.base.adapters

import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList

abstract class BaseAdapter<ModelT, ViewHolderT : RecyclerView.ViewHolder> : RecyclerView.Adapter<ViewHolderT>() {
    open var items: MutableList<ModelT> = ArrayList()

    val isEmpty: Boolean
        get() = items.isEmpty()

    override fun getItemCount(): Int {
        return items.size
    }

    open fun getItem(position: Int): ModelT? {
        // TODO: 15.03.2016 or throw runtime exception?
        return if (position < 0 || position >= items.size) null else items[position]
    }

    @JvmOverloads
    public open fun add(item: ModelT, startPosition: Int = items.size) {
        var startPosition = startPosition
        if (startPosition > items.size) {
            startPosition = items.size
        }
        val initialSize = this.items.size
        items.add(startPosition, item)
        // FIXME: 30.11.2015 [WA] https://code.google.com/p/android/issues/detail?id=77846 https://code.google.com/p/android/issues/detail?id=77232
        //http://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
        //http://stackoverflow.com/questions/26827222/how-to-change-contents-of-recyclerview-while-scrolling

        val finalStartPosition = startPosition
        if (initialSize == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemInserted(finalStartPosition)
        }

        /*Handler handler = new Handler();
		final int finalStartPosition = startPosition;
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (initialSize == 0) {
					notifyDataSetChanged();
				} else {
					notifyItemInserted(finalStartPosition);
				}
			}
		};
		handler.post(runnable);*/
    }

    open fun addAll(items: List<ModelT>) {
        //Log.d("adapter", "addAll");
        addAll(items, this.items.size)
    }

    open fun addAll(items: List<ModelT>?, startPosition: Int) {
        if (items == null || items.size == 0) {
            return
        }
        val initialSize = this.items.size
        this.items.addAll(startPosition, items)
        // FIXME: 30.11.2015 [WA] https://code.google.com/p/android/issues/detail?id=77846 https://code.google.com/p/android/issues/detail?id=77232
        //http://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
        //http://stackoverflow.com/questions/26827222/how-to-change-contents-of-recyclerview-while-scrolling

        notifyDataSetChanged()

        if (initialSize == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(startPosition, items.size)
        }

        /*Handler handler = new Handler();
		final int finalStartPosition = startPosition;
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (initialSize == 0) {
					notifyDataSetChanged();
				} else {
					notifyItemRangeInserted(finalStartPosition, items.size());
				}
			}
		};
		handler.post(runnable);*/
    }

    open fun replace(item: ModelT, position: Int): ModelT? {
        var position = position
        if (position > items.size) {
            position = items.size
        }
        val oldItem = items.removeAt(position)
        items.add(position, item)
        notifyItemChanged(position)
        return oldItem
    }

    open fun replaceAll(items: List<ModelT>) {
        clear()
        addAll(items)
    }

    open fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    open fun remove(position: Int): ModelT? {
        if (position < 0 || position >= items.size) {
            // TODO: 15.03.2016 or throw runtime exception?
            return null
        }
        val removed = items.removeAt(position)
        // FIXME: 30.11.2015 [WA] https://code.google.com/p/android/issues/detail?id=77846 https://code.google.com/p/android/issues/detail?id=77232
        //http://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position
        //http://stackoverflow.com/questions/26827222/how-to-change-contents-of-recyclerview-while-scrolling
        if (items.isEmpty()) {
            notifyDataSetChanged()
        } else {
            //			notifyItemRemoved(position);
            notifyDataSetChanged()
        }
        return removed
    }

}