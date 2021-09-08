package com.example.zrenie20.renderable

import android.content.Context
import com.example.zrenie20.data.DataItemObject
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import java.io.File

interface IArRenderObject {

    val context: Context
    val dataItemObject: DataItemObject
    //val mScene: Scene
    val renderableFile: File?

    fun pause()

    fun resume()

    fun stop()

    fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage? = null
    )

    fun getRenderable(): Renderable?

    fun setParent(parent: NodeParent)

    fun setWorldRotation(rotation: Quaternion)
}