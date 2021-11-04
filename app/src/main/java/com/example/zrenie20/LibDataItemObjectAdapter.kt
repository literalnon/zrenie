package com.example.zrenie20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.DataPackageObject
import com.example.zrenie20.data.TypeItemObjectCodeNames
import kotlinx.android.synthetic.main.item_lib_object.view.*


class LibDataItemObjectAdapter(
    val onSelectedItem: (DataItemObject) -> Unit
) :
    AbstractAdapterDelegate<Any, Any, LibDataItemObjectAdapter.VrObjectsAdapterHolder>() {

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is DataItemObject
    }

    override fun onCreateViewHolder(parent: ViewGroup): VrObjectsAdapterHolder {
        val holder = VrObjectsAdapterHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lib_object, parent, false)
        )

        return holder
    }

    override fun onBindViewHolder(
        holder: VrObjectsAdapterHolder,
        item: Any,
        items: List<Any>,
        position: Int
    ) {
        item as DataItemObject

        holder.tvLabel.text = item.id.toString()//"Label"
        holder.tvTitle.text = item.name//"Название пакета"
        holder.tvContent.text = item.description//"Краткое описание"
        holder.tvTags.text = item.type?.codeName//"#теги #теги #теги"

        Glide.with(holder.view.context)
            .load(item?.thumbnailPath)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.ivLibAr)


        holder.view.setOnClickListener {
            onSelectedItem(item)
        }

        holder.ivObjType.setImageResource(
            when(item.trigger?.type?.codeName) {
                TypeItemObjectCodeNames.GEO.codeName -> {
                    R.drawable.ic_settings_location
                }
                TypeItemObjectCodeNames.BODYPARTS.codeName -> {
                    R.drawable.ic_settings_body_part
                }
                TypeItemObjectCodeNames.SPACE.codeName -> {
                    R.drawable.ic_settings_space
                }
                TypeItemObjectCodeNames.IMAGE.codeName -> {
                    R.drawable.ic_settings_image
                }
                else -> {
                    android.R.color.transparent
                }
            }
        )
    }

    class VrObjectsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view = itemView

        var ivLibAr = itemView.ivLibAr
        var tvLabel = itemView.tvLabel
        var tvTitle = itemView.tvTitle
        var tvContent = itemView.tvContent
        var tvTags = itemView.tvTags
        var ivObjType = itemView.ivObjType
    }
}