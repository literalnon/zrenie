package com.example.zrenie20.location

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.zrenie20.LibActivity
import com.example.zrenie20.R
import com.example.zrenie20.SCREENS
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.location.arcorelocation.LocationMarker
import com.example.zrenie20.location.arcorelocation.LocationScene
import com.example.zrenie20.location.arcorelocation.rendering.LocationNodeRender
import com.example.zrenie20.location.arcorelocation.utils.ARLocationPermissionHelper
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.myarsample.BaseArActivity.Companion.BASE_MAX_SCALE
import com.example.zrenie20.myarsample.BaseArActivity.Companion.BASE_MIN_SCALE
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.example.zrenie20.renderable.ArVideoRenderObject
import com.example.zrenie20.space.FileDownloadManager
import com.example.zrenie20.space.SpaceArFragment
import com.example.zrenie20.space.VideoRecorder
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_location.ivChangeVisibility
import kotlinx.android.synthetic.main.activity_location.llFocus
import kotlinx.android.synthetic.main.activity_location.llMainActivities
import kotlinx.android.synthetic.main.activity_location.svMirror
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var installRequested = false
    private var hasFinishedLoading = false
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null

    private val anchorNode = AnchorNode()

    var videoRecorder: VideoRecorder? = null

    val VIDEO = "video"
    val PHOTO = "photo"

    var choice = PHOTO

    // Renderables for this example
    //private var exampleLayoutRenderable: ViewRenderable? = null

    // Our ARCore-Location scene
    private var locationScene: LocationScene? = null

    val dataItemArr = arrayListOf<DataItemObject>()

    var mapFragment: SupportMapFragment? = null

    var arFragment: LocationArFragment? = null
    lateinit var orientationListener: OrientationEventListener

    fun removeCurrentLocationMarker(lat: Double, lon: Double) {

        /*locationScene?.mLocationMarkers?.filter {
            it.latitude == lat && it.longitude == lon
        }?.forEach { lm ->
            lm.anchorNode?.anchor?.detach()
            lm.anchorNode?.isEnabled = false
            lm.anchorNode = null

            locationScene?.mLocationMarkers?.remove(lm)
        }*/
    }

    // CompletableFuture requires api level 24
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        //arSceneView = findViewById(R.id.ar_scene_view);
        arFragment = ar_fragment as LocationArFragment
        arSceneView = arFragment?.arSceneView!!

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapViewFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        loadData()

        // Build a renderable from a 2D View.

        arFragment?.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            Log.e("LocationScene", "hitResult : ${hitResult.hitPose} ")

            /*if (exampleLayoutRenderable != null) {
                addNodeToScene(
                    fragment = arFragment,
                    anchor = hitResult.createAnchor(),
                    renderable = videoRenderable!!
                )
            }*/
        }

        arSceneView
            ?.scene
            ?.addOnUpdateListener { frameTime: FrameTime? ->
                Log.e(
                    "LocationScene",
                    "locationScene : ${locationScene?.deviceLocation?.currentBestLocation}"
                )
                /*if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }*/
                if (locationScene == null && dataItemArr.count() > 0) {

                    locationScene = LocationScene(this, arSceneView!!)

                    //Log.e("LOCATION_FILE", "dataItemArr : ${dataItemArr.count}")
                    dataItemArr.forEach { dataItem ->
                        val lat = dataItem.trigger?.latitude?.toDoubleOrNull()
                        val lon = dataItem.trigger?.longitude?.toDoubleOrNull()

                        if (lat != null && lon != null) {
                            Log.e("LOCATION_FILE", "1 lat : ${lat}, lon : ${lon}")

                            ViewRenderable.builder()
                                .setView(this, R.layout.example_layout)
                                .build()
                                .thenApply {
                                    Log.e("LOCATION_FILE", "ViewRenderable rend")

                                    val exampleLayoutRenderable = it
                                    //hasFinishedLoading = true
                                    val exampleView: Node = Node()
                                    exampleView.renderable = exampleLayoutRenderable

                                    val layoutLocationMarker = LocationMarker(
                                        lon,
                                        lat,
                                        exampleView
                                    )

                                    layoutLocationMarker?.renderEvent = LocationNodeRender { node ->
                                        val eView = exampleLayoutRenderable?.view
                                        val markerView =
                                            eView?.findViewById<ImageView>(R.id.ivMarker)

                                        when {
                                            node.distance <= 3000 -> {
                                                markerView?.setImageResource(R.drawable.ic_less_100)
                                            }
                                            /*node.distance <= 3000 -> {
                                                markerView?.setImageResource(R.drawable.ic_less_3000)
                                            }*/
                                            else -> {
                                                markerView?.setImageResource(R.drawable.ic_logotype)
                                            }
                                        }

                                        eView?.setOnTouchListener { layoutView: View?, event: MotionEvent? ->

                                            Log.e(
                                                "LOCATION_FILE",
                                                "eView?.setOnTouchListener"
                                            )

                                            if (layoutView?.visibility != View.VISIBLE) {
                                                return@setOnTouchListener false
                                            }

                                            if (dataItem.filePath?.isNotEmpty() != true) {
                                                return@setOnTouchListener false
                                            }

                                            if (node.distance > 100) {
                                                Toast.makeText(
                                                    this,
                                                    "distance: ${node.distance}",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                return@setOnTouchListener false
                                            }

                                            Log.e(
                                                "LOCATION_FILE",
                                                "dataItem.filePath : ${dataItem.filePath}"
                                            )

                                            FileDownloadManager()
                                                .downloadFile(dataItem.filePath!!, this)
                                                ?.subscribe({ file ->
                                                    Log.e(
                                                        "LOCATION_FILE",
                                                        "file : ${file.absolutePath}"
                                                    )

                                                    Log.e(
                                                        "LOCATION_FILE",
                                                        "ViewRenderable 3 lon : ${lon}, lat : ${lat}"
                                                    )

                                                    val arRenderObject = ArRenderObjectFactory(
                                                        context = layoutView?.context!!,
                                                        dataItemObject = dataItem,
                                                        mScene = null,
                                                        renderableFile = file
                                                    ).createRenderable()

                                                    Log.e(
                                                        "LOCATION_FILE",
                                                        "dataItem : ${dataItem.filePath}"
                                                    )

                                                    arRenderObject?.start(
                                                        anchor = null,
                                                        onSuccess = {
                                                            val base = Node()
                                                            base.renderable =
                                                                arRenderObject?.getRenderable()

                                                            Log.e(
                                                                "LOCATION_FILE",
                                                                "ViewRenderable 4 lon : ${lon}, lat : ${lat}"
                                                            )

                                                            arRenderObject?.onTouchListener =
                                                                Node.OnTouchListener { hitTestResult, motionEvent ->
                                                                    Log.e(
                                                                        "LOCATION_FILE",
                                                                        "base.setOnTouchListener"
                                                                    )
                                                                    if (layoutView?.visibility != View.VISIBLE) {
                                                                        layoutView?.visibility =
                                                                            View.VISIBLE

                                                                        arRenderObject?.pause()
                                                                    } else {
                                                                        layoutView?.visibility =
                                                                            View.GONE

                                                                        arRenderObject?.resume()
                                                                    }

                                                                    false
                                                                }

                                                            val newLayoutLocationMarker =
                                                                LocationMarker(
                                                                    lon,
                                                                    lat,
                                                                    base
                                                                )

                                                            layoutView?.visibility = View.GONE

                                                            //removeCurrentLocationMarker(lat, lon)

                                                            locationScene?.mLocationMarkers?.add(
                                                                newLayoutLocationMarker!!
                                                            )
                                                        },
                                                        onFailure = {
                                                            //removeCurrentLocationMarker(lat, lon)
                                                            Toast.makeText(
                                                                this,
                                                                "failure load: error -1",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    )

                                                    arRenderObject?.setParent(arSceneView?.scene!!)

                                                    if (arRenderObject is ArVideoRenderObject) {
                                                        /*arRenderObject?.videoAnchorNode?.setOnTouchListener { hitTestResult, motionEvent ->
                                                            Log.e(
                                                                "LOCATION_FILE",
                                                                "arRenderObject?.videoAnchorNode?.setOnTouchListener"
                                                            )
                                                            arRenderObject?.pause()

                                                            return@setOnTouchListener false

                                                        }*/
                                                        setmRotation(
                                                            arRenderObject as ArVideoRenderObject,
                                                            layoutLocationMarker
                                                        )
                                                    }

                                                }, {

                                                })

                                            false
                                        }

                                    }
                                    // Adding the marker
                                    locationScene?.mLocationMarkers?.add(layoutLocationMarker!!)
                                }
                        }
                    }

                }

                val frame = arSceneView?.arFrame ?: return@addOnUpdateListener
                if (frame.camera.trackingState != TrackingState.TRACKING) {
                    return@addOnUpdateListener
                }
                if (locationScene != null) {
                    locationScene!!.processFrame(frame)
                }
                if (loadingMessageSnackbar != null) {
                    for (plane in frame.getUpdatedTrackables(
                        Plane::class.java
                    )) {
                        if (plane.trackingState == TrackingState.TRACKING) {
                            hideLoadingMessage()
                        }
                    }
                }
            }

        ivFlash?.setOnClickListener {
            Toast.makeText(this, R.string.flush, Toast.LENGTH_LONG).show()
        }

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE
                mapViewFragment.view?.visibility = View.VISIBLE

                arFragment?.arSceneView?.planeRenderer?.isEnabled = true
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE
                mapViewFragment.view?.visibility = View.GONE

                arFragment?.arSceneView?.planeRenderer?.isEnabled = false
            }
        }

        ivChangeVisibility?.performClick()

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }

        photoVideoRecorderInit()

        orientationListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(orientation: Int) {
                Log.e("listener", "onOrientationChanged : ${orientation}")

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

        ivVirtualReality.setOnClickListener {
            if (svMirror.visibility == View.GONE) {
                startBinacular(false)
            } else {
                stopBinakular(false)
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
            Log.d("AugmentedImageActivity", "Video saved: $videoPath")

            // Send  notification of updated content.
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATA, videoPath)
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    fun startBinacular(isLifecycle: Boolean) {
        if (svMirror?.visibility == View.GONE && isLifecycle) {
            return
        }

        svMirror?.visibility = View.VISIBLE

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
        if (!isLifecycle) {
            svMirror?.visibility = View.GONE
        }

        svMirror?.post {
            svMirror?.holder?.surface?.let { surface ->
                arFragment?.arSceneView?.renderer?.stopMirroring(surface)
            }
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



                Log.e("AugmentedImageActivity", "Screenshot saved in /Pictures/Screenshots")
            } else {
                Log.e("AugmentedImageActivity", "Failed to take screenshot")
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

        Log.e("AugmentedImageActivity", "mediaFile: ${mediaFile.canonicalPath}")

        val fileOutputStream = FileOutputStream(mediaFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        return mediaFile
    }

    fun setmRotation(arRenderObject: ArVideoRenderObject, layoutLocationMarker: LocationMarker) {

        /*val n = arRenderObject.videoAnchorNode
        val scale = 0.2f

        val cameraPosition: Vector3? = arSceneView?.scene?.camera?.worldPosition
        val direction = Vector3.subtract(cameraPosition, arRenderObject.videoAnchorNode.worldPosition)

        //Log.d("LocationScene", "scale " + scale);
        arRenderObject.videoAnchorNode.setWorldPosition(
            Vector3(
                arRenderObject.videoAnchorNode.worldPosition.x,
                layoutLocationMarker.height,
                arRenderObject.videoAnchorNode.worldPosition.z
            ))
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())*/
        val rotation = Quaternion()//layoutLocationMarker.node.worldRotation
        Log.e(
            "ROTATION",
            "rotation : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}"
        )

        val mPosition = layoutLocationMarker.node.worldPosition
        var mult = 1

        /*btn1.setOnClickListener {
            position.x = position.x + mult * 0.1f
            arRenderObject.videoAnchorNode.worldPosition = position
            Log.e("ROTATION", "position : ${position.x} : ${position.y} : ${position.z}")
        }

        btn2.setOnClickListener {
            position.y = position.y + mult * 0.1f
            arRenderObject.videoAnchorNode.worldPosition = position
            Log.e("ROTATION", "position : ${position.x} : ${position.y} : ${position.z}")
        }

        btn3.setOnClickListener {
            position.z = position.z + mult * 0.1f
            arRenderObject.videoAnchorNode.worldPosition = position
            Log.e("ROTATION", "position : ${position.x} : ${position.y} : ${position.z}")
        }

        btn4.setOnClickListener {
            mult *= -1
            Log.e("ROTATION", "mult : ${mult}")
        }*/
/*

        btn1.setOnClickListener {
            rotation.x = rotation.x + 0.1f
            arRenderObject.videoAnchorNode.worldRotation = rotation
            Log.e("ROTATION", "rotation1 : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}")
        }

        btn2.setOnClickListener {
            rotation.y = rotation.y + 0.1f
            arRenderObject.videoAnchorNode.worldRotation = rotation
            Log.e("ROTATION", "rotation2 : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}")
        }

        btn3.setOnClickListener {
            rotation.z = rotation.z + 0.1f
            arRenderObject.videoAnchorNode.worldRotation = rotation
            Log.e("ROTATION", "rotation3 : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}")
        }

        btn4.setOnClickListener {
            rotation.w = rotation.w + 0.1f
            arRenderObject.videoAnchorNode.worldRotation = rotation
            Log.e("ROTATION", "rotation4 : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}")
        }
*/

        rotation.x = 1f
        //rotation.y = 1f
        /*rotation.y = rotation.y + 0.5f
        rotation.z = rotation.z + 0.5f
        rotation.w = rotation.w + 0.5f*/

        arRenderObject.videoAnchorNode.worldRotation = rotation
        arRenderObject.videoAnchorNode.worldPosition = mPosition
        //arRenderObject.videoAnchorNode.localScale =
        //arRenderObject.videoAnchorNode.localRotation = lookRotation
        //arRenderObject.videoAnchorNode.worldScale = Vector3(scale, scale, scale)
    }

    fun createSession() {
        if (arSceneView?.session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val session = DemoUtils.createArSession(this, installRequested)
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this)
                    return
                } else {
                    arSceneView?.setupSession(session)
                }

                /*if (locationScene == null) {
                    // If our locationScene object hasn't been setup yet, this is a good time to do it
                    // We know that here, the AR components have been initiated.
                    locationScene = LocationScene(this, arSceneView?.session, arSceneView)
                }*/
            } catch (e: UnavailableException) {
                DemoUtils.handleSessionException(this, e)
            }
        }
    }

    /**
     * Make sure we call locationScene.resume();
     */
    override fun onResume() {
        super.onResume()
        loadData()

        if (locationScene != null) {
            locationScene!!.resume()
        }
        createSession()
        try {
            arSceneView?.resume()
        } catch (ex: CameraNotAvailableException) {
            DemoUtils.displayError(this, "Unable to get camera", ex)
            finish()
            return
        }
        if (arSceneView?.session != null) {
            showLoadingMessage()
        }

        startBinacular(true)

        orientationListener.enable()
    }

    /**
     * Make sure we call locationScene.pause();
     */
    public override fun onPause() {
        super.onPause()
        if (locationScene != null) {
            locationScene!!.pause()
        }
        arSceneView?.pause()
        stopBinakular(true)

        orientationListener.disable()
    }

    public override fun onDestroy() {
        super.onDestroy()
        arSceneView?.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, results: IntArray
    ) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this)
            } else {
                Toast.makeText(
                    this, "Camera permission is needed to run this application", Toast.LENGTH_LONG
                )
                    .show()
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Standard Android full-screen functionality.
            window
                .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar!!.isShownOrQueued) {
            return
        }
        loadingMessageSnackbar = Snackbar.make(
            findViewById(android.R.id.content),
            R.string.plane_finding,
            Snackbar.LENGTH_INDEFINITE
        )
        loadingMessageSnackbar!!.view.setBackgroundColor(-0x40cdcdce)
        loadingMessageSnackbar!!.show()
    }

    private fun hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return
        }
        loadingMessageSnackbar!!.dismiss()
        loadingMessageSnackbar = null
    }

    private var mGoogleMap: GoogleMap? = null

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        dataItemArr.forEach { dataItem ->
            val lat = dataItem.trigger?.latitude?.toDoubleOrNull()
            val lon = dataItem.trigger?.longitude?.toDoubleOrNull()

            if (lat != null && lon != null) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(dataItem.name)
                )
            }
        }

        googleMap.setMyLocationEnabled(true)

        mGoogleMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))

        mGoogleMap?.setOnMyLocationChangeListener { location ->
            val camPos = CameraPosition
                .builder(
                    mGoogleMap?.cameraPosition // current Camera
                )
                .bearing(location!!.bearing)
                .target(LatLng(location.latitude, location.longitude))
                .build()

            mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
        }

        val mapView = mapFragment?.view

        var isFullscreen = false
        val minHeight = mapView?.layoutParams?.height
        val minWidth = mapView?.layoutParams?.width
        val minMarginBottom = mapView?.marginBottom

        mGoogleMap?.setOnMapClickListener {

            if (isFullscreen) {
                mapView?.layoutParams?.height = minHeight
                mapView?.layoutParams?.width = minWidth
                (mapView?.layoutParams as? ConstraintLayout.LayoutParams)?.setMargins(
                    0,
                    0,
                    0,
                    minMarginBottom ?: 0
                )
            } else {
                mapView?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
                mapView?.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
                (mapView?.layoutParams as? ConstraintLayout.LayoutParams)?.setMargins(0, 0, 0, 0)
            }

            mapView?.requestLayout()

            isFullscreen = !isFullscreen
        }
    }

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
                //.filter { it.dataItems?.filter { it.trigger?.latitude != null } != null }

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

                //val randIndex = Math.abs(Random.nextInt()) % dataItems.count()
                //dataItems = arrayListOf<DataItemObject>().apply { add(dataItems[randIndex] ) }

                if (isNeedFilterTrigger) {
                    //items.equalTo("triggerId", SettingsActivity.currentScreen.type.id)
                    dataItems =
                        dataItems.filter {
                            //it.trigger?.typeId == SettingsActivity.currentScreen.type.id
                            it.trigger?.type?.codeName == SettingsActivity.currentScreen.type.codeName
                            //&& it?.filePath?.contains(".mp4") == true
                        }

                }

                Log.e(
                    "FileDownloadManager",
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {

                    if (dataItems.isNotEmpty()) {
                        Glide.with(this)
                            .load(activePackage?.thumbnailPath)
                            .apply(
                                RequestOptions()
                                    .transform(RoundedCorners(16))
                            )
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ivStack)
                    }

                    dataItemArr.clear()
                    dataItemArr.addAll(dataItems)
                    addItemToMap()

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
                                dataItemArr.clear()
                                dataItemArr.addAll(currentPackageItems)
                                addItemToMap()
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
            }
    }

    fun addItemToMap() {
        mGoogleMap?.clear()

        dataItemArr.forEach { dataItem ->
            val lat = dataItem.trigger?.latitude?.toDoubleOrNull()
            val lon = dataItem.trigger?.longitude?.toDoubleOrNull()

            if (lat != null && lon != null) {
                mGoogleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(dataItem.name)
                )
                mGoogleMap?.setOnMarkerClickListener {
                    //if (it.trigger?.type?.codeName == SCREENS) {
                    val gmmIntentUri: Uri =
                        Uri.parse("google.navigation:q=${it.position.latitude},${it.position.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                    return@setOnMarkerClickListener true
                    //}
                }
            }
        }

        locationScene?.clearMarkers()
        locationScene = null
    }
}