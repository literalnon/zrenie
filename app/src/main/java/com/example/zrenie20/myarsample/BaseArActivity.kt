package com.example.zrenie20.myarsample

import android.app.ActionBar
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.example.zrenie20.LibActivity
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.data.VrRenderableObject
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import com.example.zrenie20.renderable.IArRenderObject
import com.example.zrenie20.space.FileDownloadManager
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.AnimationData
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.oussaki.rxfilesdownloader.RxDownloader
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*

import android.os.HandlerThread
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import com.google.ar.core.exceptions.RecordingFailedException

import android.media.CamcorderProfile

import com.example.zrenie20.space.VideoRecorder
import android.provider.MediaStore
import android.content.ContentValues
import android.hardware.SensorManager
import android.view.*
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.zrenie20.BuildConfig
import com.example.zrenie20.R
import com.example.zrenie20.cloudAnchor2.StorageManager
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.google.ar.core.*
import com.google.ar.sceneform.assets.RenderableSource
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_my_sample.ivChangeVisibility
import kotlinx.android.synthetic.main.activity_my_sample.llFocus
import kotlinx.android.synthetic.main.activity_my_sample.llMainActivities
import kotlinx.android.synthetic.main.activity_my_sample.svMirror
import java.net.URLConnection


abstract class BaseArActivity : AppCompatActivity() {
    companion object {
        var checkedPackageId: DataPackageId? = null

        const val BASE_MIN_SCALE = 0.01f
        const val BASE_MAX_SCALE = 5f
    }

    open var isNeedCreateAnchor: Boolean = true
    open var currentRenderable: IArRenderObject? = null
    open val adapter = DelegationAdapter<Any>()

    open val cashedAssets = hashMapOf<DataItemId, IArRenderObject>()
    open var assetsArray = arrayListOf<DataItemObject>()

    abstract val layoutId: Int
    var arFragment: ArFragment? = null
    var sceneView: ArSceneView? = null

    //val vrObjectsMap = hashMapOf<DataItemObject, Node>()
    val vrObjectsMap = hashMapOf<DataItemId, Pair<DataItemId, Node>>()

    var videoRecorder: VideoRecorder? = null

    val VIDEO = "video"
    val PHOTO = "photo"

    var choice = PHOTO

    var anchorNode: AnchorNode? = null
    var node: TransformableNode? = null

    lateinit var orientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        arFragment = mArFragment as ArFragment
        sceneView = arFragment?.arSceneView
        sceneView?.planeRenderer?.isEnabled = true

        ivFlash?.setOnClickListener {
            Toast.makeText(this, R.string.flush, Toast.LENGTH_LONG).show()
        }

        /*val node = Node()
        node.setParent((arFragment as ArFragment).arSceneView.scene)
        node.setRenderable(currentRenderable)*/

        /*llContainer?.setOnTouchListener { v, event ->
            Log.e("setOnTouchListener", "event: ${event.action}")

            if (event.action == MotionEvent.ACTION_DOWN) {
                if (llFocus.visibility == View.VISIBLE) {
                    llFocus.visibility = View.GONE
                    llMainActivities.visibility = View.VISIBLE
                    return@setOnTouchListener true
                }

                if (currentRenderable?.vrRenderable == null) {
                    return@setOnTouchListener false
                }

                if (!isNeedCreateAnchor) {
                    return@setOnTouchListener true
                }

                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }*/

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            onTapArPlane(hitResult, plane, motionEvent)
        }

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE

                sceneView?.planeRenderer?.isEnabled = true
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE

                sceneView?.planeRenderer?.isEnabled = false
            }
        }

        ivChangeVisibility?.performClick()

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        rvItems.adapter = adapter
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvItems.layoutManager = layoutManager

        adapter?.manager?.addDelegate(
            VrObjectsAdapter(
                isSelectedRenderable = { vrObjectDataClass ->
                    isSelectedRenderable(vrObjectDataClass)
                },
                selectedRenderable = { vrObjectDataClass ->
                    selectedRenderable(vrObjectDataClass)
                },
                renderableUploaded = { vrObjectDataClass, renderable ->
                    renderableUploaded(vrObjectDataClass, renderable)
                },
                renderableUploadedFailed = { vrObjectDataClass ->
                    renderableUploadedFailed(vrObjectDataClass)
                },
                renderableRemoveCallback = { dataItemObject ->
                    renderableRemove(dataItemObject)
                    //node?.renderable = null
                    /* vrObjectsMap[dataItemObject.id]?.apply {
                         cashedAssets[dataItemObject.id]?.stop()
                         renderable = null
                         arFragment?.arSceneView?.scene?.removeChild(this)
                         vrObjectsMap.remove(dataItemObject.id)
                     }*/

                    //currentRenderable?.stop()
                    adapter?.notifyDataSetChanged()
                },
                isCanRemoveRenderable = {
                    isCanRemoveRenderable(it)
                }
            )
        )

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }

        videoRecorder = VideoRecorder(this)
        val orientation = resources.configuration.orientation
        videoRecorder?.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)
        videoRecorder?.setSceneView(arFragment!!.arSceneView)

        choice = PHOTO

        tvPhoto?.setOnClickListener {
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_photo_button)

            tvPhoto?.setTextColor(getColor(R.color.white))
            tvVideo?.setTextColor(getColor(R.color.grayTextColor))

            choice = PHOTO
        }

        tvVideo?.setOnClickListener {
            //toggleRecording()
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_video_button)

            tvPhoto?.setTextColor(getColor(R.color.grayTextColor))
            tvVideo?.setTextColor(getColor(R.color.white))

            choice = VIDEO
        }

        btnPhoto.setOnClickListener {
            if (choice == VIDEO) {
                toggleRecording()
            } else {
                takePhoto()
            }
        }

        //flMirror?.visibility = View.GONE

        ivArrowBack?.setOnClickListener {

            ivArrowBack?.visibility = View.GONE

            llFocus.visibility = View.GONE
            llMainActivities.visibility = View.VISIBLE

            val height = ViewGroup.LayoutParams.MATCH_PARENT

            flMirror?.visibility = View.GONE

            val mArFragmentLayoutParams = flInArFragment.layoutParams
            mArFragmentLayoutParams.height = height
            flInArFragment.layoutParams = mArFragmentLayoutParams

            ivChangeVisibility.visibility = View.VISIBLE

            stopBinakular(false)
        }

        ivVirtualReality?.setOnClickListener {
            ivArrowBack?.visibility = View.VISIBLE

            llFocus.visibility = View.GONE
            llMainActivities.visibility = View.GONE

            val height = pxFromDp(350)

            val mArFragmentLayoutParams = flInArFragment.layoutParams
            mArFragmentLayoutParams.height = height
            flInArFragment.layoutParams = mArFragmentLayoutParams

            ivChangeVisibility.visibility = View.GONE

            flMirror?.visibility = View.VISIBLE

            startBinacular(false)
        }

        photoVideoRecorderInit()

        flMirror?.post {
            flMirror?.visibility = View.GONE
        }

        orientationListener =
            object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                override fun onOrientationChanged(orientation: Int) {
                    //Log.e("listener", "onOrientationChanged : ${orientation}")

                    val newOrientation = when (orientation) {
                        in 0..45 -> {
                            360
                        }
                        in 45..135 -> {
                            270
                        }
                        in 135..225 -> {
                            180
                        }
                        in 225..315 -> {
                            90
                        }
                        in 315..360 -> {
                            360
                        }
                        else -> {
                            360
                        }
                    }.toFloat()


                    ivChangeVisibility?.rotation = newOrientation
                }
            }
    }

    open fun positionRenderableOnPlane(
        anchor: Anchor?,
        renderableCloudId: RenderableCloudId? = null
    ) {
        anchorNode = AnchorNode(anchor)
        Log.e(
            "renderable",
            "anchorNode.worldPosition : ${anchorNode?.worldPosition}, renderableCloudId : ${renderableCloudId}"
        )

        anchorNode?.setParent(arFragment?.arSceneView?.scene)
        val scale = currentRenderable?.dataItemObject?.scale?.toFloatOrNull() ?: 4f

        anchorNode?.localScale = Vector3(scale, scale, scale)

        currentRenderable?.start(
            anchor = anchor,
            onSuccess = {
                TransformableNode(arFragment?.transformationSystem)?.let { mNode ->

                    mNode.scaleController.minScale = BASE_MIN_SCALE//0.01f//Float.MIN_VALUE
                    mNode.scaleController.maxScale = BASE_MAX_SCALE//5f//Float.MAX_VALUE

                    node = mNode
                    node?.setParent(anchorNode)

                    node?.renderable = currentRenderable?.getRenderable()
                    node?.select()

                    Log.e(
                        "MainActivityBase",
                        "positionRenderableOnPlane renderableCloudId : ${renderableCloudId}, currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}"
                    )
                    vrObjectsMap[renderableCloudId ?: currentRenderable?.dataItemObject?.id!!] =
                        Pair(currentRenderable!!.dataItemObject.id!!, mNode)

                    adapter?.notifyDataSetChanged()
                }
            },
            onFailure = {
                if (currentRenderable != null && isSelectedRenderable(currentRenderable!!.dataItemObject)) {
                    renderableUploadedFailed(currentRenderable!!.dataItemObject)
                }
            }
        )
    }

    open fun onTapArPlane(hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent?) {
        val anchor = hitResult.createAnchor()
        positionRenderableOnPlane(anchor)
    }

    fun pxFromDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun startBinacular(isLifecycle: Boolean) {
        if (isLifecycle) {
            return
        }

        //svMirror?.visibility = View.VISIBLE

        svMirror?.post {
            svMirror?.holder?.surface?.let { surface ->

                arFragment?.arSceneView?.renderer?.startMirroring(
                    surface,
                    0,
                    0,
                    svMirror.width,
                    svMirror.height
                )
            }
        }
    }

    fun stopBinakular(isLifecycle: Boolean) {
        /*if (!isLifecycle) {
            svMirror?.visibility = View.GONE
        }*/

        svMirror?.post {
            svMirror?.holder?.surface?.let { surface ->
                arFragment?.arSceneView?.renderer?.stopMirroring(surface)
            }
        }
    }

    fun photoVideoRecorderInit() {
        videoRecorder = VideoRecorder(this)
        val orientation = resources.configuration.orientation
        videoRecorder?.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)
        videoRecorder?.setSceneView(arFragment!!.arSceneView)

        choice = PHOTO

        tvPhoto?.setOnClickListener {
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_photo_button)

            tvPhoto?.setTextColor(getColor(R.color.white))
            tvVideo?.setTextColor(getColor(R.color.grayTextColor))

            choice = PHOTO
        }

        tvVideo?.setOnClickListener {
            //toggleRecording()
            btnPhoto.setImageResource(com.example.zrenie20.R.drawable.ic_video_button)

            tvPhoto?.setTextColor(getColor(R.color.grayTextColor))
            tvVideo?.setTextColor(getColor(R.color.white))

            choice = VIDEO
        }

        btnPhoto.setOnClickListener {
            if (choice == VIDEO) {
                toggleRecording()
            } else {
                takePhoto()
            }
        }
    }

    fun toggleRecording() {
        val recording: Boolean = videoRecorder?.onToggleRecord() == true
        if (recording) {
            //recordButton.setImageResource(R.drawable.round_stop)
            btnPhoto.setImageResource(R.drawable.ic_video_recording_button)
            tvVideo.text = "stop"
        } else {
            tvVideo.text = "start"
            btnPhoto.setImageResource(R.drawable.ic_video_button)
            //recordButton.setImageResource(R.drawable.round_videocam)
            val videoPath = videoRecorder?.videoPath?.absolutePath
            //Toast.makeText(this, "Video saved: $videoPath", Toast.LENGTH_SHORT).show()
            Log.d("BaseArActivity", "Video saved: $videoPath")

            // Send  notification of updated content.
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATA, videoPath)
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

            shareFile(File(videoPath))
        }
    }

    fun takePhoto() {
        val view = arFragment!!.arSceneView

        // Create a bitmap the size of the scene view.
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )

        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        // Make the request to copy.
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult === PixelCopy.SUCCESS) {
                try {
                    val file = saveBitmapToDisk(bitmap)

                    shareFile(file)
                    /* val toast: Toast = Toast.makeText(
                         this, "Screenshot saved in : ${file.canonicalPath}",
                         Toast.LENGTH_LONG
                     )
                     toast.show()*/
                } catch (e: IOException) {
                    val toast: Toast = Toast.makeText(
                        this, e.toString(),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return@request
                }



                Log.e("BaseArActivity", "Screenshot saved in /Pictures/Screenshots")
            } else {
                Log.e("BaseArActivity", "Failed to take screenshot")
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    fun shareFile(file: File) {
        val intentShareFile = Intent(Intent.ACTION_SEND)

        Log.e("SHARE_FILE", "URLConnection.guessContentTypeFromName(file.name) : ${URLConnection.guessContentTypeFromName(file.name)}")
        Log.e("SHARE_FILE", "file.name : ${file.name}")

        intentShareFile.apply {
            type = URLConnection.guessContentTypeFromName(file.name)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.app_name)
            )

            putExtra(
                Intent.EXTRA_TEXT,
                "Sharing file from ${getString(R.string.app_name)}"
            )

            val fileURI = FileProvider.getUriForFile(
                Objects.requireNonNull(getApplicationContext()),
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )

            putExtra(
                Intent.EXTRA_STREAM,
                fileURI
            )
        }

        startActivity(Intent.createChooser(intentShareFile, "Share File"))
    }

    open fun saveBitmapToDisk(bitmap: Bitmap): File {

        val videoDirectory = File(
            Environment.getExternalStorageDirectory().toString() + "/Android/data/" + packageName
        )

        if (!videoDirectory.exists()) {
            videoDirectory.mkdir()
        }

        /*val videoDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Screenshots"
        )*/

        //videoDirectory.mkdir()

        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH.mm.ss")
        val formattedDate = df.format(c.time)

        val mediaFile: File = File(
            videoDirectory,
            "FieldVisualizer$formattedDate.jpeg"
        )

        try {
            mediaFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.e("BaseArActivity", "mediaFile: ${mediaFile.canonicalPath}")

        val fileOutputStream = FileOutputStream(mediaFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        return mediaFile
    }

    override fun onStart() {
        super.onStart()
        loadData()

        currentRenderable?.resume()
    }

    override fun onStop() {
        super.onStop()

        currentRenderable?.pause()
    }

    /*open fun loadData() {
        adapter.addAll(assetsArray)
    }*/

    open fun loadData() {
        val isNeedFilterTrigger = true

        val service = createService(DataItemsService::class.java)

        Log.e("FileDownloadManager", "loadData")

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                val activePackage = if (checkedPackageId == null) {
                    val ap = packages.firstOrNull()
                    checkedPackageId = ap?.id
                    ap
                } else {
                    packages.firstOrNull {
                        it.id == checkedPackageId
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
                        dataItems.filter { it.trigger?.type?.codeName == SettingsActivity.currentScreen.type.codeName }
                }
                Log.e(
                    "FileDownloadManager",
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}, checkedPackageId : ${checkedPackageId}"
                )

                if (ivStack != null && dataItems.isNotEmpty()) {
                    Glide.with(this)
                        .load(activePackage?.thumbnailPath)
                        .apply(
                            RequestOptions()
                                .transform(RoundedCorners(16))
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivStack)
                }

                assetsArray = arrayListOf<DataItemObject>().apply {
                    addAll(dataItems)
                }

                adapter.replaceAll(assetsArray)

                return@executeTransaction
                //}

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
                                "subscribe 2 checkedPackageId : ${checkedPackageId}, SettingsActivity.currentScreen.type.id : ${SettingsActivity.currentScreen.type.id}"
                            )
                            Log.e("FileDownloadManager", "subscribe 3 items : ${items.count()}")
                            Log.e(
                                "FileDownloadManager",
                                "subscribe 3 1 items : ${items.map { it?.triggerId }}"
                            )
                            val currentPackageItems = items
                                .filter {
                                    it?.dataPackageId == checkedPackageId && if (isNeedFilterTrigger) {
                                        it?.trigger?.type?.codeName == SettingsActivity.currentScreen.type.codeName
                                    } else {
                                        true
                                    }
                                }

                            Log.e(
                                "FileDownloadManager",
                                "subscribe 3 currentPackageItems : ${currentPackageItems.count()}"
                            )

                            //if (currentPackageItems.isNotEmpty()) {
                            assetsArray = arrayListOf<DataItemObject>().apply {
                                addAll(currentPackageItems)
                            }
                            adapter.replaceAll(assetsArray)
                            //}

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

    open fun isSelectedRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        Log.e(
            "renderable",
            "isSelectedRenderable : ${currentRenderable?.dataItemObject?.id} : ${dataItemObjectDataClass.id}"
        )
        return currentRenderable?.dataItemObject?.id == dataItemObjectDataClass.id
    }

    open fun isCanRemoveRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        return vrObjectsMap
            .filter {
                it.key == dataItemObjectDataClass.id
                        || it.value.first == dataItemObjectDataClass.id
            }
            .isNotEmpty()
    }

    open fun selectedRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        currentRenderable?.pause()
        currentRenderable = cashedAssets[dataItemObjectDataClass.id]
        currentRenderable?.resume()
        adapter.notifyDataSetChanged()

        return if (currentRenderable?.getRenderable() != null) {
            flProgressBar.visibility = View.GONE
            true
        } else {
            flProgressBar.visibility = View.VISIBLE
            false
        }
    }

    open fun renderableRemove(
        dataItemObject: DataItemObject? = null,
        renderableCloudId: RenderableCloudId? = null
    ) {
        Log.e(
            "MainActivity",
            "renderableRemove : ${dataItemObject?.id}, ${renderableCloudId}"
        )

        Log.e(
            "MainActivity",
            "renderableRemove : ${vrObjectsMap[dataItemObject?.id] != null}, ${vrObjectsMap[renderableCloudId] != null}"
        )


        (vrObjectsMap[renderableCloudId] ?: vrObjectsMap[dataItemObject?.id])?.apply {
            (cashedAssets[renderableCloudId] ?: cashedAssets[dataItemObject?.id])?.stop()
            second.renderable = null
            arFragment?.arSceneView?.scene?.removeChild(second)
            vrObjectsMap.remove(renderableCloudId)
            vrObjectsMap.remove(dataItemObject?.id)

            adapter.notifyDataSetChanged()
        }
    }

    open fun renderableUploaded(
        dataItemObjectDataClass: DataItemObject,
        renderable: IArRenderObject,
        anchor: Anchor? = null,
        renderableCloudId: RenderableCloudId? = null
    ) {
        flProgressBar.visibility = View.GONE

        currentRenderable = renderable
        cashedAssets[renderableCloudId ?: dataItemObjectDataClass.id!!] = renderable

        adapter.notifyDataSetChanged()

        if (anchor != null) {
            positionRenderableOnPlane(anchor, renderableCloudId)
        }
    }

    open fun renderableUploadedFailed(dataItemObjectDataClass: DataItemObject) {
        flProgressBar.visibility = View.GONE
        Toast.makeText(
            this,
            "Unable to load renderable " + dataItemObjectDataClass.filePath, Toast.LENGTH_LONG
        ).show()

        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        startBinacular(true)

        orientationListener.enable()
    }

    override fun onPause() {
        super.onPause()

        stopBinakular(true)

        orientationListener.disable()
    }

    val fileDownloadManager = FileDownloadManager()

    fun loadRenderableById(
        itemId: DataItemId,
        anchor: Anchor? = null,
        renderableCloudId: RenderableCloudId? = null
    ) {
        Log.e(
            "MainActivity",
            "loadRenderableById itemId : ${itemId},  currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}, renderableCloudId : ${renderableCloudId}"
        )
        val context = this

        val item = assetsArray.find { it.id == itemId } ?: return

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

                renderableUploaded(item, arRenderObject, anchor, renderableCloudId)

            }, {
                Log.e("renderable", "error : ${it.message}")
                Log.e("FileDownloadManager", "subscribe 2 ${it.message}")
                if (isSelectedRenderable(item)) {
                    renderableUploadedFailed(item)
                }
            })
    }
}