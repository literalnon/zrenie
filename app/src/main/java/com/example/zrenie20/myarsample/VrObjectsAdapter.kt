package com.example.zrenie20.myarsample

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.R
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.myarsample.data.VrObject
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.item_vr_object.view.*


class VrObjectsAdapter(
    val isSelectedRenderable: (VrObject) -> Boolean,
    val selectedRenderable: (VrObject) -> Boolean,
    val renderableUploaded: (VrObject, ModelRenderable) -> Unit,
    val renderableUploadedFailed: (VrObject) -> Unit,
    val renderableRemoveCallback: (VrObject) -> Unit,
    val isCanRemoveRenderable: (VrObject) -> Boolean
) :
    AbstractAdapterDelegate<Any, Any, VrObjectsAdapter.VrObjectsAdapterHolder>() {

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is VrObject
    }

    override fun onCreateViewHolder(parent: ViewGroup): VrObjectsAdapterHolder {
        val holder = VrObjectsAdapterHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vr_object, parent, false)
        )

        return holder
    }

    override fun onBindViewHolder(
        holder: VrObjectsAdapterHolder,
        item: Any,
        items: List<Any>,
        position: Int
    ) {
        val vrDataClass = item as VrObject

        if (isCanRemoveRenderable(vrDataClass)) {
            holder.ivClose.visibility = View.VISIBLE
        } else {
            holder.ivClose.visibility = View.GONE
        }

        if (isSelectedRenderable(vrDataClass)) {
            holder.viewSelected.setBackgroundResource(R.drawable.yellow_rounded_background)
        } else {
            holder.viewSelected.setBackgroundResource(android.R.color.transparent)
        }

        holder.ivClose.setOnClickListener {
            renderableRemoveCallback(item)
            holder.ivClose.visibility = View.GONE
        }

        holder.tvName.text = item.name
        holder.view.setOnClickListener { view ->
            val context = view.context

            Log.e("renderable", "ModelRenderable.builder() 1, ${item.link}, ${item.link.contains("a")}, ${item.link.contains("11")}")

            if (!selectedRenderable(item)) {
                var mRenderable: ModelRenderable? = null

                /*if (item.link.contains("a.glb")) {
                    val raw = when {
                        item.link.contains("11") -> {
                            R.raw.i7
                        }
                        item.link.contains("12") -> {
                            R.raw.poly
                        }
                        item.link.contains("13") -> {
                            R.raw.s15a
                        }
                        item.link.contains("14") -> {
                            R.raw.zombie_fast
                        }
                        else -> {
                            R.raw.andy_dance
                        }
                    }

                    ModelRenderable.builder()
                        .setSource(
                            context, raw
                        )
                } else {*/
                    ModelRenderable.builder()
                        .setSource(
                            context, Uri.parse(vrDataClass.link))
                            /*RenderableSource.builder().setSource(
                            context,
                            Uri.parse(vrDataClass.link),
                            RenderableSource.SourceType.GLB
                        )
                            .setScale(0.5f) // Scale the original model to 50%.
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build()
                        )*/
                //}
                        .setIsFilamentGltf(true)
                    //.setRegistryId(vrDataClass.link)
                    .build()
                    .thenAccept { renderable: ModelRenderable ->
                        Log.e("renderable", "ModelRenderable.builder() 5")
                        if (isSelectedRenderable(item)) {
                            mRenderable = renderable
                            renderableUploaded(item, renderable)
                        }
                    }
                    .exceptionally { throwable: Throwable? ->
                        Log.e("renderable", "ModelRenderable.builder() 6")
                        if (isSelectedRenderable(item)) {
                            renderableUploadedFailed(item)
                        }
                        null
                    }


            }
        }
    }

    class VrObjectsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view = itemView
        var viewSelected = itemView.viewSelected
        var ivClose = itemView.ivClose
        var ivObject = itemView.ivObject
        var tvName = itemView.tvName
    }
}
