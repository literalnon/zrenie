package com.example.zrenie20

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zrenie20.common.helpers.SnackbarHelper
import com.example.zrenie20.myarsample.nodes.AugmentedImageNode
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture


/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
class AugmentedImageFragment : ArFragment() {

    private val augmentedImageMap: MutableMap<AugmentedImage, AugmentedImageNode?> =
        HashMap<AugmentedImage, AugmentedImageNode?>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Turn off the plane discovery since we're only looking for images
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = arSceneView.session?.config
        config?.augmentedImageDatabase = setupAugmentedImageDatabase(arSceneView.session)
        arSceneView.session?.configure(config)

        arSceneView
            .scene
            .addOnUpdateListener { frameTime ->
                Log.e("AUGMENTED_IMAGE", "frame addOnUpdateListener : ${arSceneView.arFrame}")

                val frame = arSceneView.arFrame
                val augmentedImages = frame
                    ?.getUpdatedTrackables(AugmentedImage::class.java) ?: arrayListOf()

                Log.e("AUGMENTED_IMAGE", "frame augmentedImages : ${augmentedImages.size}")

                for (augmentedImage in augmentedImages) {
                    Log.e("AUGMENTED_IMAGE", "frame trackingState ${augmentedImage.trackingState}")
                    when (augmentedImage.trackingState) {
                        TrackingState.PAUSED -> {
                            // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                            // but not yet tracked.
                            val text = "Detected Image " + augmentedImage.index
                            SnackbarHelper().showMessage(activity, text)
                        }
                        TrackingState.TRACKING -> {

                            // Create a new anchor for newly found images.
                            if (!augmentedImageMap.containsKey(augmentedImage)) {
                                /*val node = AugmentedImageNode(context)
                                node.image = augmentedImage
                                augmentedImageMap[augmentedImage] = node
                                arSceneView.scene.addChild(node)*/

                                val renderable: CompletableFuture<ModelRenderable> =
                                    ModelRenderable.builder()
                                        .setSource(
                                            context,
                                            Uri.parse("file:///android_asset/aImage/i6.glb")
                                            /*
                                            RenderableSource.builder().setSource(
                                                context,
                                                Uri.parse("file:///android_asset/aImage/i6.glb"),
                                                RenderableSource.SourceType.GLB
                                            )
                                                .setScale(0.03f) // Scale the original model to 50%.
                                                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                                .build()
                                                */
                                        )
                                        .setRegistryId(augmentedImage.name)
                                        .build()


                                // Set the anchor based on the center of the image.
                                val anchor = AnchorNode()
                                anchor.anchor =
                                    augmentedImage.createAnchor(augmentedImage.centerPose)

                                // Create the transformable andy and add it to the anchor.
                                val node = TransformableNode(transformationSystem)
                                node.setParent(anchor)
                                node.renderable = renderable.getNow(null)
                                node.select()
                            }
                        }
                        TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
                    }
                }
            }
    }


    override fun getSessionConfiguration(session: Session): Config {
        Log.e("AUGMENTED_IMAGE", "setting getSessionConfiguration")

        val config = super.getSessionConfiguration(session)
        config.focusMode = Config.FocusMode.AUTO

        /*if (!setupAugmentedImageDatabase(config, session)) {
            SnackbarHelper().showError(activity, "Could not setup augmented image database")
        }*/

        //session.configure(config)
        return config
    }

    private fun setupAugmentedImageDatabase(session: Session?): AugmentedImageDatabase {
        //val assetManager = (if (context != null) requireContext().assets else null) ?: return false

        val augmentedImageBitmap = loadAugmentedImageBitmap()

        val augmentedImageDatabase: AugmentedImageDatabase = AugmentedImageDatabase(session)
        for (i in augmentedImageBitmap.indices) {
            augmentedImageDatabase.addImage("image_name_$i", augmentedImageBitmap[i])
        }

        Log.e(
            "AUGMENTED_IMAGE",
            "setting setupAugmentedImageDatabase : ${augmentedImageBitmap.size}, ${augmentedImageDatabase.numImages}"
        )

        //config.augmentedImageDatabase = augmentedImageDatabase

        return augmentedImageDatabase
    }

    private fun loadAugmentedImageBitmap(): ArrayList<Bitmap?> {
        val bitmaps: ArrayList<Bitmap?> = ArrayList()
        val bitmapsName: ArrayList<String> = ArrayList()

        bitmapsName.add("triggers/it1.jpeg")
        bitmapsName.add("triggers/it2.jpeg")
        bitmapsName.add("triggers/it3.jpeg")
        bitmapsName.add("triggers/it4.jpeg")
        bitmapsName.add("triggers/it5.jpeg")
        bitmapsName.add("triggers/it6.jpeg")
        bitmapsName.add("triggers/it7.jpeg")

        for (i in bitmapsName.indices) {
            try {
                context?.assets?.open(bitmapsName[i])
                    .use { `is` ->
                        bitmaps.add(BitmapFactory.decodeStream(`is`))
                    }

            } catch (e: IOException) {
                Log.e("AUGMENTED IMAGE fragment", "IO exception loading augmented image bitmap.", e)
            }
        }
        return bitmaps
    }

    private var shouldConfigureSession = true
    private var session: Session? = null

    private fun configureSession() {
        session = arSceneView.session
        val config = Config(session)

        config.augmentedImageDatabase = setupAugmentedImageDatabase(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session!!.configure(config)
    }

    override fun onResume() {
        super.onResume()

        if (shouldConfigureSession) {
            configureSession()
            shouldConfigureSession = false
            arSceneView.setupSession(session)
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session?.resume()
            arSceneView.resume()
        } catch (e: CameraNotAvailableException) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            session = null
            return
        }
    }
}