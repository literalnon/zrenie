package com.example.zrenie20.location

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import com.example.zrenie20.LibActivity
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.location.arcorelocation.LocationMarker
import com.example.zrenie20.location.arcorelocation.LocationScene
import com.example.zrenie20.location.arcorelocation.rendering.LocationNodeRender
import com.example.zrenie20.location.arcorelocation.utils.ARLocationPermissionHelper
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.example.zrenie20.renderable.ArVideoRenderObject
import com.example.zrenie20.space.FileDownloadManager
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

    private val anchorNode = AnchorNode()

    // Renderables for this example
    private var exampleLayoutRenderable: ViewRenderable? = null

    // Our ARCore-Location scene
    private var locationScene: LocationScene? = null

    val dataItemArr = arrayListOf<DataItemObject>()

    var mapFragment: SupportMapFragment? = null

    var arFragment: LocationArFragment? = null

    // CompletableFuture requires api level 24
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        //arSceneView = findViewById(R.id.ar_scene_view);
        arFragment = ar_fragment as LocationArFragment
        arSceneView = arFragment?.arSceneView!!

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        loadData()

        // Build a renderable from a 2D View.
        val exampleLayout = ViewRenderable.builder()
            .setView(this, R.layout.example_layout)
            .build()
            .thenApply {
                exampleLayoutRenderable = it
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

                    dataItemArr.forEach { dataItem ->
                        val lat = dataItem.trigger?.latitude?.toDoubleOrNull()
                        val lon = dataItem.trigger?.longitude?.toDoubleOrNull()

                        if (lat != null && lon != null) {
                            val layoutLocationMarker = LocationMarker(
                                lon,
                                lat,
                                exampleView
                            )

                            layoutLocationMarker?.renderEvent = LocationNodeRender { node ->
                                val eView = exampleLayoutRenderable?.view
                                val markerView = eView?.findViewById<ImageView>(R.id.ivMarker)

                                when {
                                    node.distance <= 100 -> {
                                        markerView?.setImageResource(R.drawable.ic_less_100)
                                    }
                                    node.distance <= 3000 -> {
                                        markerView?.setImageResource(R.drawable.ic_less_3000)
                                    }
                                    else -> {
                                        markerView?.setImageResource(R.drawable.ic_logotype)
                                    }
                                }

                                eView?.setOnTouchListener { v: View?, event: MotionEvent? ->
                                    Toast.makeText(
                                        this, "exampleView Location marker touched.", Toast.LENGTH_LONG
                                    ).show()

                                    if (dataItem.filePath?.isNotEmpty() != true){
                                        return@setOnTouchListener false
                                    }
                                    Log.e("DOWNLOAD_VIDEO_FILE", "dataItem.filePath : ${dataItem.filePath}")

                                    FileDownloadManager().downloadFile(dataItem.filePath!!, this)
                                        ?.subscribe({ file ->
                                            Log.e("DOWNLOAD_VIDEO_FILE", "file : ${file.absolutePath}")

                                            val arRenderObject = ArRenderObjectFactory(
                                                context = v?.context!!,
                                                dataItemObject = dataItem,
                                                mScene = null,
                                                renderableFile = file
                                            ).createRenderable()

                                            arRenderObject?.start(
                                                anchor = null,
                                                        onSuccess = {
                                                            val base = Node()
                                                            base.renderable = arRenderObject?.getRenderable()

                                                            val newLayoutLocationMarker =
                                                                LocationMarker(
                                                                    lon,
                                                                    lat,
                                                                    base
                                                                )

                                                            locationScene?.mLocationMarkers?.add(
                                                                newLayoutLocationMarker!!
                                                            )
                                                        },
                                                        onFailure = {}
                                            )

                                            arRenderObject?.setParent(arSceneView?.scene!!)
                                            setmRotation(arRenderObject as ArVideoRenderObject, layoutLocationMarker)
                                            //arRenderObject?.setParent(layoutLocationMarker?.anchorNode)
                                            //(arRenderObject as ArVideoRenderObject)?.videoAnchorNode?.anchor = layoutLocationMarker?.anchorNode?.anchor
                                            //(arRenderObject as ArVideoRenderObject)?.videoAnchorNode?.worldPosition = layoutLocationMarker?.anchorNode?.worldPosition
                                            //(arRenderObject as ArVideoRenderObject)?.videoAnchorNode?.localPosition = layoutLocationMarker?.anchorNode?.localPosition

                                            /*if (file.absolutePath.contains(".mp4")) {
                                                val base = getVideoRenderableNode(file)
                                                val newLayoutLocationMarker =
                                                    LocationMarker(
                                                        lon,
                                                        lat,
                                                        base
                                                    )

                                                locationScene?.mLocationMarkers?.add(
                                                    newLayoutLocationMarker!!
                                                )
                                                *//*locationScene?.mLocationMarkers?.remove(
                                                    layoutLocationMarker!!
                                                )*//*
                                            } else {
                                                ModelRenderable.builder()
                                                    .setSource(
                                                        this,
                                                        RenderableSource
                                                            .builder()
                                                            .setSource(
                                                                this,
                                                                file.toUri(),
                                                                RenderableSource.SourceType.GLB
                                                            )
                                                            .setScale(0.5f)
                                                            .setRecenterMode(RenderableSource.RecenterMode.NONE)
                                                            .build()
                                                    )
                                                    .build()
                                                    .thenAccept { renderable: ModelRenderable ->
                                                        val base = Node()
                                                        base.renderable = renderable

                                                        val newLayoutLocationMarker =
                                                            LocationMarker(
                                                                lon,
                                                                lat,
                                                                base
                                                            )

                                                        locationScene?.mLocationMarkers?.add(
                                                            newLayoutLocationMarker!!
                                                        )
                                                        *//*locationScene?.mLocationMarkers?.remove(
                                                            layoutLocationMarker!!
                                                        )*//*

                                                    }
                                                    .exceptionally { throwable: Throwable? ->
                                                        Log.e(
                                                            "FileDownloadManager",
                                                            "ModelRenderable.builder() 6 : ${throwable?.message}"
                                                        )

                                                        null
                                                    }
                                            }*/
                                        }, {

                                        })

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

                            }
                            // Adding the marker
                            locationScene?.mLocationMarkers?.add(layoutLocationMarker!!)
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

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }
        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        //ARLocationPermissionHelper.requestPermission(this)
    }// Add  listeners etc here

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
        Log.e("ROTATION", "rotation : ${rotation.x} : ${rotation.y} : ${rotation.z} : ${rotation.w}")

        val position = layoutLocationMarker.node.worldPosition
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

        rotation.x = rotation.x + 1f
        /*rotation.y = rotation.y + 0.5f
        rotation.z = rotation.z + 0.5f
        rotation.w = rotation.w + 0.5f*/

        arRenderObject.videoAnchorNode.worldRotation = rotation
        arRenderObject.videoAnchorNode.worldPosition = position
        //arRenderObject.videoAnchorNode.localScale =
        //arRenderObject.videoAnchorNode.localRotation = lookRotation
        //arRenderObject.videoAnchorNode.worldScale = Vector3(scale, scale, scale)
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

                if (isNeedFilterTrigger) {
                    //items.equalTo("triggerId", SettingsActivity.currentScreen.type.id)
                    dataItems =
                        dataItems.filter {
                            //it.trigger?.typeId == SettingsActivity.currentScreen.type.id
                            it.trigger?.type?.codeName == SettingsActivity.currentScreen.type.codeName
                                    && it?.filePath?.contains(".mp4") == true
                        }

                }

                Log.e(
                    "FileDownloadManager",
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {
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
        dataItemArr.forEach { dataItem ->
            val lat = dataItem.trigger?.latitude?.toDoubleOrNull()
            val lon = dataItem.trigger?.longitude?.toDoubleOrNull()

            if (lat != null && lon != null) {
                mGoogleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(dataItem.name)
                )
            }
        }

        locationScene = null
    }
}