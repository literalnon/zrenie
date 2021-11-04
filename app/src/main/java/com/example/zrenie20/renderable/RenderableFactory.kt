package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.SCREENS
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.TypeItemObjectCodeNames
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import java.io.File

interface IArRenderObjectFactory {
    fun createRenderable(): IArRenderObject
}

class ArRenderObjectFactory(
    private val context: Context,
    private val dataItemObject: DataItemObject,
    private val mScene: Scene? = null,
    var renderableFile: File? = null
) : IArRenderObjectFactory {
    override fun createRenderable(): IArRenderObject {
        /*return ArWebViewRenderObject(
            context = context,
            dataItemObject = dataItemObject,
            //mScene = mScene,
            renderableFile = renderableFile
        )*/

        return ArAlphaVideoRenderObject(
            context = context,
            dataItemObject = dataItemObject,
            //mScene = mScene,
            renderableFile = renderableFile
        )

        if (renderableFile == null) {
            return ArLoadingRenderObject(
                context = context,
                dataItemObject = dataItemObject,
                //mScene = mScene,
                renderableFile = renderableFile
            )
        }

        return when (dataItemObject.type?.codeName) {
            TypeItemObjectCodeNames.VIDEO.codeName -> {
                ArVideoRenderObject(
                    context = context,
                    dataItemObject = dataItemObject,
                    //mScene = mScene,
                    renderableFile = renderableFile!!
                )
            }
            TypeItemObjectCodeNames.IMAGE.codeName -> {
                if (SettingsActivity.currentScreen == SCREENS.LOCATION) {
                    ArImageRenderObjectForLocation(
                        context = context,
                        dataItemObject = dataItemObject,
                        renderableFile = renderableFile!!
                    )
                } else {
                    ArImageRenderObject(
                        context = context,
                        dataItemObject = dataItemObject,
                        //mScene = mScene,
                        renderableFile = renderableFile!!
                    )
                }
            }
            else -> {
                ArGlbRenderObject(
                    context = context,
                    dataItemObject = dataItemObject,
                    //mScene = mScene,
                    renderableFile = renderableFile!!
                )
            }
        }
    }
}
