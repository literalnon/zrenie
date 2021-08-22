package com.example.zrenie20.augmentedimage

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import com.example.zrenie20.space.FileDownloadManager
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

open class ArVideoFragment : ArFragment() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var externalTexture: ExternalTexture
    private lateinit var videoRenderable: ModelRenderable
    private lateinit var videoAnchorNode: AnchorNode

    private var activeAugmentedImage: AugmentedImage? = null
    private var mapOfAugmentedImageNode = hashMapOf<AugmentedImage, AugmentedImageNode>()
    private val anchorNode = AnchorNode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer()
    }

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
        createArScene()

        return view
    }

    /*val paths: ArrayList<String> = arrayListOf(
        "it1.png",
        "face/it2.png",
        "it3.png",
        "it4.jpeg",
        "it5.jpeg",
        "it6.jpeg",
        "it7.jpeg"
    )*/

    /*val links: ArrayList<String> = arrayListOf(
        "i1.mp4",
        "i2.mp4",
        "i3.mp4",
        "i4.mp4",
        "i5.mp4",
        "i6.glb",
        "i7a.glb"
    )*/

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

    override fun onResume() {
        super.onResume()

    }

    private fun loadAugmentedImageBitmap(): HashMap<String, Bitmap?> {

        /*assetsArray.forEach { itemObject ->
            try {
                Glide.with(requireContext())
                    .asBitmap()
                    .load(itemObject.trigger?.filePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            bitmaps[itemObject.trigger?.filePath ?: ""] = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e)
            }
        }*/
        return bitmaps
    }

    open fun loadData() {
        val isNeedFilterTrigger = false

        val service = createService(DataItemsService::class.java)

        Log.e("FileDownloadManager", "loadData")

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                val activePackage = if (BaseArActivity.checkedPackageId == null) {
                    val ap = packages.firstOrNull()
                    BaseArActivity.checkedPackageId = ap?.id
                    ap
                } else {
                    packages.firstOrNull {
                        it.id == BaseArActivity.checkedPackageId
                    }
                }

                Log.e(
                    "FileDownloadManager",
                    "loadData 1 activePackage?.dataItems?.isNotEmpty() : ${activePackage?.dataItems?.isNotEmpty()}"
                )


                val items = realm.where(RealmDataItemObject::class.java)
                    .equalTo("dataPackageId", activePackage?.id)

                var dataItems = items.findAll()
                    .map { it.toDataItemObject() }

                if (isNeedFilterTrigger) {
                    //items.equalTo("triggerId", SettingsActivity.currentScreen.type.id)
                    dataItems =
                        dataItems.filter { it.trigger?.typeId == SettingsActivity.currentScreen.type.id }
                }
                Log.e(
                    "FileDownloadManager",
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {
                    assetsArray = arrayListOf<DataItemObject>().apply {
                        addAll(dataItems)
                    }

                    initializeSession()

                    return@executeTransaction
                }

                val observable =
                    /*Observable.fromIterable<DataPackageObject>(packages)
                    .flatMap { packageObject ->*/
                    service.getEntryTypes()//packageObject.id.toString()
                        //}
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ items ->
                            Log.e(
                                "FileDownloadManager",
                                "subscribe 2 checkedPackageId : ${BaseArActivity.checkedPackageId}, SettingsActivity.currentScreen.type.id : ${SettingsActivity.currentScreen.type.id}"
                            )
                            Log.e("FileDownloadManager", "subscribe 3 items : ${items.count()}")
                            Log.e(
                                "FileDownloadManager",
                                "subscribe 3 1 items : ${items.map { it?.triggerId }}"
                            )
                            val currentPackageItems = items
                                .filter {
                                    it?.dataPackageId == BaseArActivity.checkedPackageId && if (isNeedFilterTrigger) {
                                        it?.trigger?.type?.id == SettingsActivity.currentScreen.type.id
                                    } else {
                                        true
                                    }
                                }

                            Log.e(
                                "FileDownloadManager",
                                "subscribe 3 currentPackageItems : ${currentPackageItems.count()}"
                            )

                            if (currentPackageItems.isNotEmpty()) {
                                assetsArray = arrayListOf<DataItemObject>().apply {
                                    addAll(currentPackageItems)
                                }
                                initializeSession()
                            }

                            realm
                                .executeTransaction { realm ->

                                    realm.delete(RealmDataItemObject::class.java)

                                    items.map {
                                        realm.copyToRealm(it.toRealmDataItemObject())
                                    }
                                }
                        }, {
                            Log.e("FileDownloadManager", "subscribe 4 error : ${it.message}")

                            it.printStackTrace()
                        })


                /*assetsArray = arrayListOf<DataItemObject>().apply {
                    addAll(firstPackage
                        ?.dataItems
                        ?.map {
                            it.toDataItemObject()
                        } ?: listOf()
                    )
                }*/

                //adapter.addAll(assetsArray)
            }
    }

    override fun getSessionConfiguration(session: Session): Config {

        /*fun loadAugmentedImageBitmap(imageName: String): Bitmap =
            requireContext().assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
            try {
                config.augmentedImageDatabase = AugmentedImageDatabase(session).also { db ->
                    db.addImage(TEST_VIDEO_1, loadAugmentedImageBitmap(TEST_IMAGE_1))
                    db.addImage(TEST_VIDEO_2, loadAugmentedImageBitmap(TEST_IMAGE_2))
                    db.addImage(TEST_VIDEO_3, loadAugmentedImageBitmap(TEST_IMAGE_3))
                }
                return true
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Could not add bitmap to augmented image database", e)
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e)
            }
            return false
        }*/

        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED

        if (!setupAugmentedImageDatabase(config, session)) {
            Toast.makeText(
                requireContext(),
                "Could not setup augmented image database",
                Toast.LENGTH_LONG
            ).show()
        }

        mConfig = config

        return config

        /*return super.getSessionConfiguration(session).also {
            mConfig = it
            it.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            it.focusMode = Config.FocusMode.AUTO

            if (!setupAugmentedImageDatabase(it, session)) {
                Toast.makeText(
                    requireContext(),
                    "Could not setup augmented image database",
                    Toast.LENGTH_LONG
                ).show()
            }
        }*/
    }

    var mConfig: Config? = null

    private fun createArScene() {
        // Create an ExternalTexture for displaying the contents of the video.
        externalTexture = ExternalTexture().also {
            mediaPlayer.setSurface(it.surface)
            //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        }

        // Create a renderable with a material that has a parameter of type 'samplerExternal' so that
        // it can display an ExternalTexture.
        ModelRenderable.builder()
            .setSource(requireContext(), R.raw.augmented_video_model)
            .build()
            .thenAccept { renderable ->
                videoRenderable = renderable
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false
                renderable.material.setExternalTexture("videoTexture", externalTexture)
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }

        videoAnchorNode = anchorNode.apply {
            setParent(arSceneView.scene)
        }
    }

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
                    if (videoAnchorNode is AugmentedImageNode) {
                        //Log.e("AUGMENTED_IMAGE", "videoAnchorNode is AugmentedImageNode")
                        (videoAnchorNode as AugmentedImageNode).pauseImage()
                    } else {
                        //Log.e("AUGMENTED_IMAGE", "else")
                        videoAnchorNode.renderable = null

                        if (isArVideoPlaying()) {
                            pauseArVideo()
                        }
                    }
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
            if (arItem?.type?.id == "2") {//activeAugmentedImage.index < 5) {
                if (fullTrackingImages.any { it.index == activeAugmentedImage.index }) {
                    if (!isArVideoPlaying()) {
                        resumeArVideo()
                    }

                    return
                }
            } else {
                if (fullTrackingImages.any { it.index == activeAugmentedImage.index }) {

                    mapOfAugmentedImageNode[activeAugmentedImage]?.let { activeVideoAnchorNode ->
                        //mapOfAugmentedImageNode[activeAugmentedImage]?.resumeImage()
                        val augmentedImageNode = activeVideoAnchorNode
                        videoAnchorNode = augmentedImageNode
                        augmentedImageNode.setImage(activeAugmentedImage)
                        augmentedImageNode.resumeImage()
                    }
                    return
                }
            }
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
                Log.e("AUGMENTED_IMAGE", "activeAugmentedImage?.name : ${activeAugmentedImage?.name}")
                Log.e("AUGMENTED_IMAGE", "assetsArray : ${Gson().toJson(assetsArray)}")

                if (mArItem?.type?.id == "2") {
                    mediaPlayer.reset()
                }

                fileDownloadManager.downloadFile(mArItem?.filePath!!, requireContext())
                    .subscribe({ renderableFile ->

                        Log.e("AUGMENTED_IMAGE", "arItem?.type?.id : ${mArItem?.type?.id}")

                        if (mArItem?.type?.id == "2") {
                            playbackArVideo(augmentedImage, renderableFile)
                        } else {
                            //pauseArVideo()
                            Log.e("AUGMENTED_IMAGE", "create AugmentedImageNode")
                            //if (mapOfAugmentedImageNode[augmentedImage] == null) {
                            mapOfAugmentedImageNode[augmentedImage] =
                                AugmentedImageNode(context, augmentedImage, renderableFile).apply {
                                    //setImage(augmentedImage)
                                    arSceneView.scene.addChild(this)
                                }
                            //}

                            val augmentedImageNode = mapOfAugmentedImageNode[augmentedImage]!!
                            videoAnchorNode = augmentedImageNode
                            augmentedImageNode.setImage(augmentedImage)
                            augmentedImageNode.resumeImage()

                            //augmentedImageMap[augmentedImage] = node
                        }
                    }, {
                        it.printStackTrace()
                    })

            } catch (e: Exception) {
                Log.e(TAG, "Could not play video [${augmentedImage.name}]", e)
            }
        }
    }

    private fun isArVideoPlaying() = mediaPlayer.isPlaying

    private fun pauseArVideo() {
        videoAnchorNode.renderable = null
        mediaPlayer.pause()
    }

    private fun resumeArVideo() {
        videoAnchorNode = anchorNode
        mediaPlayer.start()
        videoAnchorNode.renderable = videoRenderable
    }

    private fun dismissArVideo() {
        videoAnchorNode.anchor?.detach()
        videoAnchorNode.renderable = null
        activeAugmentedImage = null
        mediaPlayer.reset()
    }

    val fileDownloadManager = FileDownloadManager()

    private fun playbackArVideo(augmentedImage: AugmentedImage, renderableFile: File) {
        Log.d(TAG, "playbackVideo = ${augmentedImage.name}")
        Log.e("AUGMENTED_IMAGE", "playbackArVideo : ${augmentedImage.name}")
        //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        //val arItem = assetsArray.find { it.trigger?.filePath == activeAugmentedImage?.name }

        Log.e("AUGMENTED_IMAGE", "renderableFile.absolutePath : ${renderableFile.absolutePath}")
        mediaPlayer.reset()
        mediaPlayer.setDataSource(renderableFile.absolutePath)

        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.start()

        videoAnchorNode = anchorNode
        videoAnchorNode.anchor?.detach()
        videoAnchorNode.anchor = augmentedImage.createAnchor(augmentedImage.centerPose)
        videoAnchorNode.localScale = Vector3(
            augmentedImage.extentX, // width
            1.0f,
            augmentedImage.extentZ * mediaPlayer.videoHeight / mediaPlayer.videoWidth
        ) // height

        //activeAugmentedImage = augmentedImage
        //Log.e("VIDEO_TRACK", "mediaPlayer.videoHeight : ${mediaPlayer.videoHeight}, mediaPlayer.videoWidth : ${mediaPlayer.videoWidth}")
        //Log.e("VIDEO_TRACK", "mediaPlayer.videoHeight : ${externalTexture.surfaceTexture.}, mediaPlayer.videoWidth : ${mediaPlayer.videoWidth}")
        externalTexture.surfaceTexture.setOnFrameAvailableListener {
            it.setOnFrameAvailableListener(null)
            videoAnchorNode.renderable = videoRenderable
        }


        /*requireContext().assets.openFd(augmentedImage.name)
            .use { descriptor ->
                mediaPlayer.reset()
                mediaPlayer.setDataSource(descriptor)
            }.also {
                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()
            }*/


    }

    override fun onPause() {
        super.onPause()
        dismissArVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {
        private const val TAG = "ArVideoFragment"

        val bitmaps: HashMap<String, Bitmap?> = hashMapOf()
        open var assetsArray = arrayListOf<DataItemObject>()
    }
}