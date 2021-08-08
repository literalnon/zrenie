package com.example.zrenie20.location

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zrenie20.R
import com.example.zrenie20.location.arcorelocation.LocationMarker
import com.example.zrenie20.location.arcorelocation.LocationScene
import com.example.zrenie20.location.arcorelocation.rendering.LocationNodeRender
import com.example.zrenie20.location.arcorelocation.utils.ARLocationPermissionHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_sceneform.*


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var installRequested = false
    private var hasFinishedLoading = false
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null

    // Renderables for this example
    private var andyRenderable: Renderable? = null
    private var exampleLayoutRenderable: ViewRenderable? = null

    // Our ARCore-Location scene
    private var locationScene: LocationScene? = null

    var longitude1 = 39.684878//39.694841//47.269713//47.262300//47.269713
    var longitude2 = 39.689366//39.684101//47.262300//47.269713
    var latitude1 = 47.260584//47.258984//39.677922//39.682470
    var latitude2 = 47.260719//47.257441//39.682470

    // CompletableFuture requires api level 24
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sceneform)
        //arSceneView = findViewById(R.id.ar_scene_view);
        val arFragment: LocationArFragment = ar_fragment as LocationArFragment
        arSceneView = arFragment.arSceneView

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
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



        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        /*val andy = ModelRenderable.builder()
            .setSource(this, R.raw.andy)
            .build()*/

        /*val andy = ModelRenderable.builder()
            .setSource(
                this,
                //Uri.parse(resource)
                RenderableSource.builder().setSource(
                    this,
                    Uri.parse("file:///android_asset/aImage/i2.glb"),//"file:///android_asset/aImage/i6.glb"),
                    RenderableSource.SourceType.GLB
                )
                    .setScale(0.5f) // Scale the original model to 50%.
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            //.setRegistryId(augmentedImage.name)
            .build()
            .thenApply {
                andyRenderable = it
                hasFinishedLoading = true
                it
            }*/


        arFragment?.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            Log.e("LocationScene", "hitResult : ${hitResult.hitPose} ")
            /*if (andyRenderable != null) {
                addNodeToScene(
                    fragment = arFragment,
                    anchor = hitResult.createAnchor(),
                    renderable = andyRenderable!!
                )
            }*/

            if (exampleLayoutRenderable != null) {
                addNodeToScene(
                    fragment = arFragment,
                    anchor = hitResult.createAnchor(),
                    renderable = exampleLayoutRenderable!!
                )
            }
        }
        /*CompletableFuture.allOf(
            exampleLayout,
            andy
        )
            .handle<Any?> { notUsed: Void?, throwable: Throwable? ->
                // When you build a Renderable, Sceneform loads its resources in the background while
                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                // before calling get().
                if (throwable != null) {
                    DemoUtils.displayError(this, "Unable to load renderables", throwable)
                    return@handle null
                }
                try {
                    exampleLayoutRenderable = exampleLayout.get()
                    andyRenderable = andy.get()
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                    DemoUtils.displayError(this, "Unable to load renderables", ex)
                } catch (ex: ExecutionException) {
                    DemoUtils.displayError(this, "Unable to load renderables", ex)
                }
                null
            }*/

        /*arSceneView
            ?.scene
            ?.addOnUpdateListener {
                if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }

                *//*if (locationScene == null) {
                    // If our locationScene object hasn't been setup yet, this is a good time to do it
                    // We know that here, the AR components have been initiated.
                    locationScene = LocationScene(this, arSceneView)
                }*//*
            }*/

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.

        arSceneView
            ?.scene
            ?.addOnUpdateListener{ frameTime: FrameTime? ->
                Log.e("LocationScene", "locationScene : ${locationScene?.deviceLocation?.currentBestLocation}")
                if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }
                if (locationScene == null) {
                    // If our locationScene object hasn't been setup yet, this is a good time to do it
                    // We know that here, the AR components have been initiated.
                    locationScene = LocationScene(this, arSceneView!!)
                    //createSession()
                    //locationScene = LocationScene(this, arSceneView?.session, arSceneView)
                    // Now lets create our location markers.
                    // First, a layout
                    val layoutLocationMarker = LocationMarker(
                        longitude1,
                        latitude1,
                        exampleView
                    )

                    // An example "onRender" event, called every frame
                    // Updates the layout with the markers distance
                    layoutLocationMarker.renderEvent = LocationNodeRender { node ->
                        val eView = exampleLayoutRenderable?.view
                        val distanceTextView = eView?.findViewById<TextView>(R.id.textView2)
                        distanceTextView?.text = node.distance.toString() + "M"
                    }
                    // Adding the marker
                    locationScene?.mLocationMarkers?.add(layoutLocationMarker)

                    // Adding a simple location marker of a 3D model
                    locationScene?.mLocationMarkers?.add(
                        LocationMarker(
                            longitude2,
                            latitude2,
                            mAndy
                        )
                    )
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


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        //ARLocationPermissionHelper.requestPermission(this)
    }// Add  listeners etc here

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
                    c, "Location marker touched.", Toast.LENGTH_LONG
                )
                    .show()
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
            }
            return base
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

    override fun onMapReady(googleMap: GoogleMap) {
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
    }
}