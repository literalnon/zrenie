package com.example.zrenie20.augmentedimage

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.zrenie20.R
import com.example.zrenie20.data.*
import com.example.zrenie20.renderable.ArLoadingRenderObject
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.example.zrenie20.renderable.IArRenderObject
import com.example.zrenie20.space.FileDownloadManager
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_location.*
import kotlin.collections.HashMap

open class AugmentedImageFragment : ArFragment() {

    private var activeAugmentedImage: AugmentedImage? = null
    private var mapOfAugmentedImageNode = hashMapOf<AugmentedImage, AugmentedImageNode>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        arSceneView.isLightEstimationEnabled = false

        initializeSession()

        return view
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
        val augmentedImageBitmap = loadAugmentedImageBitmap()
        if (augmentedImageBitmap.isEmpty()) {
            return false
        }

        var augmentedImageDatabase: AugmentedImageDatabase = AugmentedImageDatabase(session)

        augmentedImageBitmap.forEach { (name, bitmap) ->
            augmentedImageDatabase.addImage(name, bitmap)
        }

        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    private fun loadAugmentedImageBitmap(): HashMap<String, Bitmap?> {
        return bitmaps
    }

    override fun getSessionConfiguration(session: Session): Config {

        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED

        if (!setupAugmentedImageDatabase(config, session)) {
            Toast.makeText(
                requireContext(),
                "Пакет не выбран",
                Toast.LENGTH_LONG
            ).show()
        }

        mConfig = config

        return config
    }

    var mConfig: Config? = null
    private val fileDownloadManager = FileDownloadManager()

    /**
     * In this case, we want to support the playback of one video at a time.
     * Therefore, if ARCore loses current active image FULL_TRACKING we will pause the video.
     * If the same image gets FULL_TRACKING back, the video will resume.
     * If a new image will become active, then the corresponding video will start from scratch.
     */
    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame ?: return

        //Log.e("AUGMENTED_IMAGE", "onUpdate")

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        val arItem = assetsArray.find { it.trigger?.filePath == activeAugmentedImage?.name }

        //Log.e("AUGMENTED_IMAGE", "onUpdate 1: ${arItem}, ${activeAugmentedImage?.name}")

        // If current active augmented image isn't tracked anymore and video playback is started - pause video playback
        val nonFullTrackingImages =
            updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }
        activeAugmentedImage?.let { activeAugmentedImage ->
            //Log.e("AUGMENTED_IMAGE", "activeAugmentedImage let")
            try {
                if (nonFullTrackingImages.any { it.index == activeAugmentedImage.index }) {
                    //Log.e("AUGMENTED_IMAGE", "it.index == activeAugmentedImage.index")
                    //arRenderObject?.pause()
                    mapOfAugmentedImageNode[activeAugmentedImage]?.pauseImage()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AUGMENTED_IMAGE", "error ${e.message}")
            }
            activeAugmentedImage
            //Log.e("AUGMENTED_IMAGE", "activeAugmentedImage index != activeAugmentedImage.index")
        }

        val fullTrackingImages =
            updatedAugmentedImages.filter { it.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING }
        if (fullTrackingImages.isEmpty()) return

        // If current active augmented image is tracked but video playback is paused - resume video playback
        activeAugmentedImage?.let { activeAugmentedImage ->
            /*if (arItem?.type?.codeName == TypeItemObjectCodeNames.VIDEO.codeName) {//activeAugmentedImage.index < 5) {
                if (fullTrackingImages.any { it.index == activeAugmentedImage.index }) {
                    arRenderObject?.resume()

                    return
                }
            } else {*/
                if (fullTrackingImages.any { it.index == activeAugmentedImage.index }) {

                    mapOfAugmentedImageNode[activeAugmentedImage]?.let { activeVideoAnchorNode ->
                        val augmentedImageNode = activeVideoAnchorNode
                        //videoRenderable?.videoAnchorNode = augmentedImageNode
                        //augmentedImageNode.setImage(activeAugmentedImage)
                        augmentedImageNode.resumeImage()
                    }
                    return
                }
            //}
        }

        // Otherwise - make the first tracked image active and start video playback
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            try {
                Log.e(
                    TAG,
                    "activeAugmentedImage?.name : ${augmentedImage?.name}, ${augmentedImage?.index}"
                )
                activeAugmentedImage = augmentedImage

                /*Log.e("AUGMENTED_IMAGE", "arItem?.type?.id : ${arItem?.type?.id}")
                Log.e("AUGMENTED_IMAGE", "arItem?.type?.id : ${Gson().toJson(arItem)}")
                Log.e("AUGMENTED_IMAGE", "arItem?.filePath : ${arItem?.filePath}")*/
                val mArItem = assetsArray.find { it.trigger?.filePath == activeAugmentedImage?.name }
                Log.e("AUGMENTED_IMAGE", "arItem?.filePath : ${mArItem?.filePath}")
                Log.e("AUGMENTED_IMAGE", "arItem?.filePath : ${arItem?.filePath}")
                Log.e("AUGMENTED_IMAGE", "mArItem : ${Gson().toJson(mArItem)}")
                Log.e("AUGMENTED_IMAGE", "arItem : ${arItem}")
                Log.e(
                    "AUGMENTED_IMAGE",
                    "activeAugmentedImage?.name : ${activeAugmentedImage?.name}"
                )
                Log.e("AUGMENTED_IMAGE", "assetsArray : ${Gson().toJson(assetsArray)}")

                if (mArItem?.type?.codeName == TypeItemObjectCodeNames.VIDEO.codeName) {
                    mapOfAugmentedImageNode[activeAugmentedImage]?.stop()
                }

                mArItem?.let {
                    Log.e("AUGMENTED_IMAGE", "create loader")

                    mapOfAugmentedImageNode[augmentedImage] = AugmentedImageNode(
                        context = requireContext(),
                        augmentedImage = augmentedImage,
                        renderableFile = null,
                        dataItemObject = mArItem,
                        mScene = arSceneView.scene
                    ).apply {
                        setImage(augmentedImage)
                        arSceneView.scene.addChild(this)
                    }
                }

                fileDownloadManager.downloadFile(mArItem?.filePath!!, requireContext())
                    .subscribe({ renderableFile ->

                        Log.e("AUGMENTED_IMAGE", "arItem?.type?.id : ${mArItem?.type?.id}")

                        Log.e("AUGMENTED_IMAGE", "create AugmentedImageNode : ${mapOfAugmentedImageNode[augmentedImage]?.renderableFile == null}")
                        if (mapOfAugmentedImageNode[augmentedImage]?.renderableFile == null) {
                            arSceneView.scene.removeChild(mapOfAugmentedImageNode[augmentedImage])
                            mapOfAugmentedImageNode[augmentedImage] =
                                AugmentedImageNode(
                                    context = requireContext(),
                                    augmentedImage = augmentedImage,
                                    renderableFile = renderableFile,
                                    dataItemObject = mArItem,
                                    mScene = arSceneView.scene
                                ).apply {
                                    setImage(augmentedImage)
                                    arSceneView.scene.addChild(this)
                                }

                            //val augmentedImageNode = mapOfAugmentedImageNode[augmentedImage]!!
                            //augmentedImageNode.setImage(augmentedImage)
                        }
                    }, {
                        it.printStackTrace()
                    })

            } catch (e: Exception) {
                Log.e(TAG, "Could not play video [${augmentedImage.name}]", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //dismissArVideo()
        mapOfAugmentedImageNode[activeAugmentedImage]?.pauseImage()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapOfAugmentedImageNode[activeAugmentedImage]?.stop()
    }

    companion object {
        private const val TAG = "ArVideoFragment"

        val bitmaps: HashMap<String, Bitmap?> = hashMapOf()
        open var assetsArray = arrayListOf<DataItemObject>()
    }
}