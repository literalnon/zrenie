package com.example.zrenie20.location

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.marginBottom
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.augmentedimage.ArVideoFragment
import com.example.zrenie20.location.arcorelocation.LocationMarker
import com.example.zrenie20.location.arcorelocation.LocationScene
import com.example.zrenie20.location.arcorelocation.rendering.LocationNodeRender
import com.example.zrenie20.location.arcorelocation.utils.ARLocationPermissionHelper
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_location.ivChangeVisibility
import kotlinx.android.synthetic.main.activity_location.llFocus
import kotlinx.android.synthetic.main.activity_location.llMainActivities
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var installRequested = false
    private var hasFinishedLoading = false
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null

    /*private lateinit var mediaPlayer: MediaPlayer
    private lateinit var externalTexture: ExternalTexture
    private lateinit var videoRenderable: ModelRenderable*/

    // Renderables for this example
    private var andyRenderable: Renderable? = null
    private var exampleLayoutRenderable: ViewRenderable? = null

    // Our ARCore-Location scene
    private var locationScene: LocationScene? = null

    var longitude1 = 39.684878//39.694841//47.269713//47.262300//47.269713
    var longitude2 = 39.689366//39.684101//47.262300//47.269713
    var latitude1 = 47.260584//47.258984//39.677922//39.682470
    var latitude2 = 47.260719//47.257441//39.682470

    var mapFragment: SupportMapFragment? = null
    //var videoLocationmarker: LocationMarker? = null
    var arFragment: LocationArFragment? = null
    var layoutLocationMarker: LocationMarker? = null

    // CompletableFuture requires api level 24
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        //mediaPlayer = MediaPlayer()
        createArScene()
        //arSceneView = findViewById(R.id.ar_scene_view);
        arFragment = ar_fragment as LocationArFragment
        arSceneView = arFragment?.arSceneView!!

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // Build a renderable from a 2D View.
        val exampleLayout = ViewRenderable.builder()
            .setView(this, R.layout.example_layout)
            .build()
            .thenApply {
                exampleLayoutRenderable = it
                hasFinishedLoading = true
            }

        ViewRenderable.builder()
            .setView(this, R.layout.example_layout_2)
            .build()
            .thenApply {
                andyRenderable = it
                hasFinishedLoading = true
            }

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
                if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }
                if (locationScene == null) {
                    locationScene = LocationScene(this, arSceneView!!)

                    layoutLocationMarker = LocationMarker(
                        longitude1,
                        latitude1,
                        exampleView
                    )

                    layoutLocationMarker?.renderEvent = LocationNodeRender { node ->
                        val eView = exampleLayoutRenderable?.view
                        val distanceTextView = eView?.findViewById<TextView>(R.id.textView2)
                        distanceTextView?.text = node.distance.toString() + "M"
                    }
                    // Adding the marker
                    locationScene?.mLocationMarkers?.add(layoutLocationMarker!!)

                    // Adding a simple location marker of a 3D model
                    /*locationScene?.mLocationMarkers?.add(
                        LocationMarker(
                            longitude2,
                            latitude2,
                            mAndy
                        )
                    )*/

                    /*videoNode = Node()
                    //videoNode.renderable = videoRenderable

                    videoNode.setOnTapListener { v: HitTestResult?, event: MotionEvent? ->
                        Toast.makeText(
                            this, "videoRenderable touched.", Toast.LENGTH_LONG
                        )
                            .show()

                        val videoNode1 = Node()
                        videoNode1.renderable = videoRenderable

                        val videoLocationmarker1 = LocationMarker(
                            longitude2,
                            latitude2,
                            videoNode1
                        )

                        locationScene?.mLocationMarkers?.add(
                            videoLocationmarker1
                        )
                        //playbackArVideo()
                    }

                    videoLocationmarker = LocationMarker(
                        longitude2,
                        latitude2,
                        videoNode
                    )

                    locationScene?.mLocationMarkers?.add(
                        videoLocationmarker!!
                    )
                    */
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

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE
            }
        }

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        //ARLocationPermissionHelper.requestPermission(this)
    }// Add  listeners etc here

    private fun createArScene() {
        // Create an ExternalTexture for displaying the contents of the video.
        /*externalTexture = ExternalTexture().also {
            mediaPlayer.setSurface(it.surface)
            //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        }*/

        // Create a renderable with a material that has a parameter of type 'samplerExternal' so that
        // it can display an ExternalTexture.
        /*ModelRenderable.builder()
            .setSource(this, R.raw.augmented_video_model)
            .build()
            .thenAccept { renderable ->
                videoRenderable = renderable
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false
                renderable.material.setExternalTexture("videoTexture", externalTexture)
            }
            .exceptionally { throwable ->
                Log.e("ArVideoFragment.TAG", "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }*/

        /*ModelRenderable.builder()
            .setSource(
                this,
                //Uri.parse(resource)
                RenderableSource.builder().setSource(
                    this,
                    Uri.parse("file:///android_asset/face/f1.glb"),
                    RenderableSource.SourceType.GLB
                )
                    .setRecenterMode(RenderableSource.RecenterMode.NONE)
                    .build()
            )
            //.setRegistryId(augmentedImage.name)
            .build()
            .thenApply { renderable ->
                videoRenderable = renderable
            }*/

        /*videoAnchorNode = anchorNode.apply {
            setParent(arSceneView?.scene)
        }*/
    }

    private fun playbackArVideo() {
        //Log.d("ArVideoFragment.TAG", "playbackVideo = ${augmentedImage.name}")
        //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

        /*this.assets.openFd("i1.mp4")
            .use { descriptor ->
                mediaPlayer.reset()
                mediaPlayer.setDataSource(descriptor)
            }.also {
                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()
            }*/

        /*val videoNode = Node()
        videoNode.renderable = videoRenderable

        videoLocationmarker = LocationMarker(
            longitude,
            latitude,
            videoNode
        )

        videoLocationmarker?.scalingMode = LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN
        locationScene?.mLocationMarkers?.add(videoLocationmarker!!)*/
        /*videoAnchorNode = anchorNode
        videoAnchorNode.anchor?.detach()
        videoAnchorNode.anchor = hitTest?.node?.crea*/


        //activeAugmentedImage = augmentedImage
        //Log.e("VIDEO_TRACK", "mediaPlayer.videoHeight : ${mediaPlayer.videoHeight}, mediaPlayer.videoWidth : ${mediaPlayer.videoWidth}")
        //Log.e("VIDEO_TRACK", "mediaPlayer.videoHeight : ${externalTexture.surfaceTexture.}, mediaPlayer.videoWidth : ${mediaPlayer.videoWidth}")
        /*externalTexture.surfaceTexture.setOnFrameAvailableListener {
            it.setOnFrameAvailableListener(null)
        }*/
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val node = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(node)
        fragment.arSceneView.scene.addChild(node)
        transformableNode.select()
    }

    /**
     * Example node of a layout
     *
     * @return
     */
    private val exampleView: Node
        private get() {
            val base = Node()
            base.renderable = exampleLayoutRenderable
            val c: Context = this
            // Add  listeners etc here
            val eView = exampleLayoutRenderable?.view
            eView?.setOnTouchListener { v: View?, event: MotionEvent? ->
                Toast.makeText(
                    c, "exampleView Location marker touched.", Toast.LENGTH_LONG
                ).show()

                /*Log.e("NULL", "videoLocationmarker 1 : ${videoLocationmarker}")
                Log.e("NULL", "videoLocationmarker 2: ${videoLocationmarker?.anchorNode}")
                Log.e("NULL", "videoLocationmarker 3 : ${videoLocationmarker?.anchorNode?.anchor}")
                Log.e(
                    "NULL",
                    "layoutLocationMarker 3 : ${layoutLocationMarker?.anchorNode?.anchor}"
                )

                val videoNode1 = Node()
                videoNode1.renderable = videoRenderable*/

                /*val videoLocationmarker1 = LocationMarker(
                    longitude2,
                    latitude2,
                    videoNode1
                )

                locationScene?.mLocationMarkers?.add(
                    videoLocationmarker1
                )*/
                /*addNodeToScene(
                    fragment = arFragment!!,
                    anchor = videoLocationmarker?.anchorNode?.anchor!!,
                    renderable = videoRenderable!!
                )

                //locationScene?.mLocationMarkers?.add(videoLocationmarker!!)

                playbackArVideo()*/

                false
            }
            return base
        }

    /***
     * Example Node of a 3D model
     *
     * @return
     */
    private val mAndy: Node
        private get() {
            val base = Node()
            base.renderable = andyRenderable
            val c: Context = this
            base.setOnTapListener { v: HitTestResult?, event: MotionEvent? ->
                Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG
                )
                    .show()

                playbackArVideo(

                )
            }
            return base
        }

    /*private var videoNode: Node = Node()
        private get() {
            val base = Node()
            base.renderable = videoRenderable
            val c: Context = this

            base.setOnTapListener { v: HitTestResult?, event: MotionEvent? ->
                Toast.makeText(
                    c, "videoRenderable touched.", Toast.LENGTH_LONG
                )
                    .show()

                playbackArVideo()
            }
            return base
        }*/

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

        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude1, longitude1))
                .title("Marker 1")
        )

        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude2, longitude2))
                .title("Marker 2")
        )

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
}