package com.example.zrenie20.myarsample

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.zrenie20.R
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.example.zrenie20.renderable.IArRenderObject
import com.example.zrenie20.space.FileDownloadManager
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.oussaki.rxfilesdownloader.RxDownloader
import kotlinx.android.synthetic.main.item_vr_object.view.*
import java.io.File


class VrObjectsAdapter(
    val isSelectedRenderable: (DataItemObject) -> Boolean,
    val selectedRenderable: (DataItemObject) -> Boolean,
    val renderableUploaded: (DataItemObject, IArRenderObject) -> Unit,
    val renderableUploadedFailed: (DataItemObject) -> Unit,
    val renderableRemoveCallback: (DataItemObject) -> Unit,
    val isCanRemoveRenderable: (DataItemObject) -> Boolean
) : AbstractAdapterDelegate<Any, Any, VrObjectsAdapter.VrObjectsAdapterHolder>() {

    val fileDownloadManager = FileDownloadManager()

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is DataItemObject
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
        val vrDataClass = item as DataItemObject

        Glide.with(holder.ivObject)
            .load(vrDataClass.thumbnailPath)
            .apply(
                RequestOptions()
                    .transform(RoundedCorners(16))
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.ivObject)

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

        //holder.tvName.text = item.name
        holder.view.setOnClickListener { view ->
            val context = view.context

            Log.e(
                "renderable",
                "ModelRenderable.builder() 1, ${item.filePath}, ${item.filePath?.contains("a")}, ${
                    item.filePath?.contains("11")
                }"
            )

            if (!selectedRenderable(item)) {
                var mRenderable: ModelRenderable? = null

                val recentMode =
                    if (vrDataClass.filePath?.contains("f1") == true ||
                        vrDataClass.filePath?.contains("f2") == true ||
                        vrDataClass.filePath?.contains("f3") == true || true
                    ) {
                        RenderableSource.RecenterMode.NONE
                    } else {
                        RenderableSource.RecenterMode.ROOT
                    }

                fileDownloadManager.downloadFile(item.filePath!!, context)
                    .subscribe({ file ->
                        Log.e("renderable", "item.filePath : ${item.filePath}")

                        val arRenderObject = ArRenderObjectFactory(
                            context = context,
                            dataItemObject = item,
                            mScene = null,
                            renderableFile = file
                        ).createRenderable()

                        Log.e("renderable", "isSelectedRenderable : ${isSelectedRenderable(item)}")
                        //if (isSelectedRenderable(item)) {
                            renderableUploaded(item, arRenderObject)
                        //}

                        //selectedRenderable(item)


                        /*ModelRenderable.builder()
                            .setSource(
                                context,
                                RenderableSource
                                    .builder()
                                    .setSource(
                                        context,
                                        file.toUri(),
                                        RenderableSource.SourceType.GLB
                                    )
                                    .setScale(0.5f)
                                    .setRecenterMode(recentMode)
                                    .build()
                            )
                            .build()
                            .thenAccept { renderable: ModelRenderable ->
                                Log.e("FileDownloadManager", "ModelRenderable.builder() 5")
                                if (isSelectedRenderable(item)) {
                                    mRenderable = renderable
                                    renderableUploaded(item, renderable)
                                }
                            }
                            .exceptionally { throwable: Throwable? ->
                                Log.e("FileDownloadManager", "ModelRenderable.builder() 6 : ${throwable?.message}")
                                if (isSelectedRenderable(item)) {
                                    renderableUploadedFailed(item)
                                }
                                null
                            }*/
                    }, {
                        Log.e("renderable", "error : ${it.message}")
                        Log.e("FileDownloadManager", "subscribe 2 ${it.message}")
                        if (isSelectedRenderable(item)) {
                            renderableUploadedFailed(item)
                        }
                    })




            }
        }
    }

    class VrObjectsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view = itemView
        var viewSelected = itemView.viewSelected
        var ivClose = itemView.ivClose
        var ivObject = itemView.ivObject
        //var tvName = itemView.tvName
    }
}
