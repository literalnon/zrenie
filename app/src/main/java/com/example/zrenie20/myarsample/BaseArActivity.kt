package com.example.zrenie20.myarsample

import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
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
import android.view.PixelCopy

import android.os.HandlerThread
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import com.google.ar.core.exceptions.RecordingFailedException

import com.google.ar.core.RecordingConfig
import com.google.ar.core.Session
import android.media.CamcorderProfile

import com.example.zrenie20.space.VideoRecorder
import android.provider.MediaStore

import android.content.ContentValues
import com.example.zrenie20.R
import com.example.zrenie20.binakular.BinacularActivity


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
    val vrObjectsMap = hashMapOf<DataItemObject, Node>()

    var videoRecorder: VideoRecorder? = null

    val VIDEO = "video"
    val PHOTO = "photo"

    var choice = PHOTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        arFragment = mArFragment as ArFragment
        sceneView = arFragment?.arSceneView
        sceneView?.planeRenderer?.isEnabled = false

        ivFlash?.setOnClickListener {
            Log.e("FLASH", "ivFlash setOnClickListener")

            sceneView?.pause()

            val camManager = getSystemService(CAMERA_SERVICE) as CameraManager

            var cameraId: String? = null
            if (camManager != null) {
                camManager.cameraIdList?.forEach {
                    Log.e("FLASH", "cameraIdList : ${it}")
                }

                cameraId = camManager.cameraIdList[0]
                Log.e("FLASH", "cameraId : ${cameraId}")

                camManager.setTorchMode(cameraId, true)
            }

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

        var anchorNode: AnchorNode? = null
        var node: TransformableNode? = null

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->

            /*if (currentRenderable == null) {
                return@setOnTapArPlaneListener
            }

            if (!isNeedCreateAnchor) {
                return@setOnTapArPlaneListener
            }*/

            //node?.let { arFragment?.arSceneView?.scene?.removeChild(node) }
            //anchorNode?.let { arFragment?.arSceneView?.scene?.removeChild(anchorNode) }
            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            //val newAnchor = AnchorNode()
            //newAnchor.worldPosition = Vector3(-0.068282515f, -0.6458561f, -0.46753782f)
            anchorNode = AnchorNode(anchor)
            Log.e("renderable", "anchorNode.worldPosition : ${anchorNode?.worldPosition}")
            //anchorNode?.worldPosition = Vector3(-0.068282515f, -0.6458561f, -0.46753782f)
            //anchorNode = newAnchor
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
                        //currentRenderable?.setParent(node!!)
                        node?.renderable = currentRenderable?.getRenderable()
                        node?.select()

                        //currentRenderable?.dataItemObject?.let {
                        vrObjectsMap[currentRenderable?.dataItemObject!!] = mNode
                        //}

                        adapter?.notifyDataSetChanged()
                    }
                },
                onFailure = {
                    if (currentRenderable != null && isSelectedRenderable(currentRenderable!!.dataItemObject)) {
                        renderableUploadedFailed(currentRenderable!!.dataItemObject)
                    }
                }
            )

            // Create the transformable andy and add it to the anchor.

        }

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE
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
                    //node?.renderable = null
                    vrObjectsMap[dataItemObject]?.apply {
                        cashedAssets[dataItemObject.id]?.stop()
                        renderable = null
                        arFragment?.arSceneView?.scene?.removeChild(this)
                        vrObjectsMap.remove(dataItemObject)
                    }

                    //currentRenderable?.stop()
                    adapter?.notifyDataSetChanged()
                },
                isCanRemoveRenderable = {
                    vrObjectsMap[it] != null
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

        ivVirtualReality?.setOnClickListener {
            startActivity(Intent(this, BinacularActivity::class.java))
        }
        photoVideoRecorderInit()
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
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {
                    Glide.with(this)
                        .load(activePackage?.thumbnailPath)
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

    open fun renderableUploaded(
        dataItemObjectDataClass: DataItemObject,
        renderable: IArRenderObject
    ) {
        flProgressBar.visibility = View.GONE

        currentRenderable = renderable
        cashedAssets[dataItemObjectDataClass.id!!] = renderable

        adapter.notifyDataSetChanged()
    }

    open fun renderableUploadedFailed(dataItemObjectDataClass: DataItemObject) {
        flProgressBar.visibility = View.GONE
        Toast.makeText(
            this,
            "Unable to load renderable " + dataItemObjectDataClass.filePath, Toast.LENGTH_LONG
        ).show()

        adapter.notifyDataSetChanged()
    }
}