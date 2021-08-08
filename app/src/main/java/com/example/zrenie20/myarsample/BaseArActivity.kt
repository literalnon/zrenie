package com.example.zrenie20.myarsample

import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.LibActivity
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.data.DataItemId
import com.example.zrenie20.myarsample.data.DataItemObject
import com.example.zrenie20.myarsample.data.VrRenderableObject
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.AnimationData
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*

abstract class BaseArActivity : AppCompatActivity() {
    open var isNeedCreateAnchor: Boolean = true
    open var currentRenderable: VrRenderableObject? = null
    open val adapter = DelegationAdapter<Any>()

    open val cashedAssets = hashMapOf<DataItemId, VrRenderableObject>()
    open var assetsArray = arrayListOf<DataItemObject>()

    abstract val layoutId: Int
    var arFragment: ArFragment? = null
    var sceneView: ArSceneView? = null
    val vrObjectsMap = hashMapOf<DataItemObject, Node>()
    private val colors = Arrays.asList(
        Color(0f, 0f, 0f, 1f),
        Color(1f, 0f, 0f, 1f),
        Color(0f, 1f, 0f, 1f),
        Color(0f, 0f, 1f, 1f),
        Color(1f, 1f, 0f, 1f),
        Color(0f, 1f, 1f, 1f),
        Color(1f, 0f, 1f, 1f),
        Color(1f, 1f, 1f, 1f)
    )
    private var nextColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        arFragment = mArFragment as ArFragment
        sceneView = arFragment?.arSceneView

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

            if (currentRenderable?.vrRenderable == null) {
                return@setOnTapArPlaneListener
            }

            if (!isNeedCreateAnchor) {
                return@setOnTapArPlaneListener
            }

            //node?.let { arFragment?.arSceneView?.scene?.removeChild(node) }
            //anchorNode?.let { arFragment?.arSceneView?.scene?.removeChild(anchorNode) }
            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val newAnchor = AnchorNode()
            newAnchor.worldPosition = Vector3(-0.068282515f, -0.6458561f, -0.46753782f)
            anchorNode = AnchorNode(anchor)
            Log.e("CLOUD_ANCHORS", "anchorNode.worldPosition : ${anchorNode?.worldPosition}")
            //anchorNode?.worldPosition = Vector3(-0.068282515f, -0.6458561f, -0.46753782f)
            //anchorNode = newAnchor
            anchorNode?.setParent(arFragment?.arSceneView?.scene)

            // Create the transformable andy and add it to the anchor.
            TransformableNode(arFragment?.transformationSystem)?.let { mNode ->
                node = mNode
                node?.setParent(anchorNode)
                node?.renderable = currentRenderable?.vrRenderable
                node?.select()

                currentRenderable?.dataItemObject?.let {
                    vrObjectsMap[it] = mNode
                }

                adapter?.notifyDataSetChanged()
            }

            Log.e("ANIMATION_VR", "${currentRenderable?.vrRenderable?.animationDataCount}")
            if ((currentRenderable?.vrRenderable?.animationDataCount ?: 0) > 0) {
                val data: AnimationData? = currentRenderable?.vrRenderable?.getAnimationData(0)
                val animator = ModelAnimator(data, currentRenderable?.vrRenderable)
                animator.start()
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
                renderableRemoveCallback = {
                    //node?.renderable = null
                    vrObjectsMap[it]?.apply {
                        renderable = null
                        arFragment?.arSceneView?.scene?.removeChild(this)
                        vrObjectsMap.remove(it)
                    }

                    adapter?.notifyDataSetChanged()
                },
                isCanRemoveRenderable = {
                    vrObjectsMap[it] != null
                }
            )
        )

        loadData()

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }
    }

    open fun loadData() {
        adapter.addAll(assetsArray)
    }

    open fun isSelectedRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        return currentRenderable?.dataItemObject?.id == dataItemObjectDataClass.id
    }

    open fun selectedRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        currentRenderable = cashedAssets[dataItemObjectDataClass.id] ?: VrRenderableObject(
            dataItemObject = dataItemObjectDataClass,
            vrRenderable = null
        )

        adapter.notifyDataSetChanged()

        return if (currentRenderable?.vrRenderable != null) {
            flProgressBar.visibility = View.GONE
            true
        } else {
            flProgressBar.visibility = View.VISIBLE
            false
        }
    }

    open fun renderableUploaded(dataItemObjectDataClass: DataItemObject, renderable: ModelRenderable) {
        flProgressBar.visibility = View.GONE

        VrRenderableObject(
            dataItemObject = dataItemObjectDataClass,
            vrRenderable = renderable
        ).let { item ->
            currentRenderable = item
            cashedAssets[dataItemObjectDataClass.id] = item
        }

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