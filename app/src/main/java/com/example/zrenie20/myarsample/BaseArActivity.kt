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
import com.example.zrenie20.myarsample.data.VrObject
import com.example.zrenie20.myarsample.data.VrObjectId
import com.example.zrenie20.myarsample.data.VrRenderableObject
import com.google.android.filament.gltfio.Animator
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

private class AnimationInstance internal constructor(
    var animator: Animator?,
    index: Int,
    var startTime: Long
) {
    var duration: Float?
    var index: Int

    init {
        duration = animator?.getAnimationDuration(index)
        this.index = index
    }
}

abstract class BaseArActivity : AppCompatActivity() {
    open var isNeedCreateAnchor: Boolean = true
    open var currentRenderable: VrRenderableObject? = null
    open val adapter = DelegationAdapter<Any>()

    open val cashedAssets = hashMapOf<VrObjectId, VrRenderableObject>()
    open var assetsArray = arrayListOf<VrObject>()
    private val animators: ArrayList<AnimationInstance> = arrayListOf()
    /*VrObject(
        id = 0,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/1.glb"
    ),
    VrObject(
        id = 1,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/2.glb"
    ),
    VrObject(
        id = 2,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/3.glb"
    ),
    VrObject(
        id = 3,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/4.glb"
    ),
    VrObject(
        id = 4,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/5.glb"
    ),
    VrObject(
        id = 5,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/6.glb"
    ),
    VrObject(
        id = 6,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/Eagle%20fbx%20to%20glb.glb"
    ),
    VrObject(
        id = 7,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/8.glb"
    ),
    VrObject(
        id = 8,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/eagle.glb"
    ),
    VrObject(
        id = 9,
        link = "https://drive.google.com/u/0/uc?id=1GVtgZU03z9ZtDWHh7WWjw9P0Q7RErUql&export=download"
    ),
    VrObject(
        id = 11,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/11.glb"
    ),
    VrObject(
        id = 12,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/12.glb"
    ),
    VrObject(
        id = 13,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/13.glb"
    ),
    VrObject(
        id = 14,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/14.glb"
    ),
    VrObject(
        id = 15,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/15.glb"
    ),
    VrObject(
        id = 16,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/16.glb"
    ),
    VrObject(
        id = 17,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/17.glb"
    ),
    VrObject(
        id = 18,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/18.glb"
    ),
    VrObject(
        id = 19,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/19.glb"
    ),
    VrObject(
        id = 21,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/21.glb"
    ),
    VrObject(
        id = 22,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/22.glb"
    ),
    VrObject(
        id = 23,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/23.glb"
    ),
    VrObject(
        id = 24,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/24.glb"
    ),
    VrObject(
        id = 25,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/25.glb"
    ),
    VrObject(
        id = 26,
        link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/26.glb"
    )
)*/

    abstract val layoutId: Int
    var arFragment: ArFragment? = null
    var sceneView: ArSceneView? = null
    val vrObjectsMap = hashMapOf<VrObject, Node>()
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

            /*if(NoobCameraManager.getInstance().isFlashOn){
                NoobCameraManager.getInstance().turnOffFlash();
            }else{
                NoobCameraManager.getInstance().turnOnFlash();
            }*/

            /*val cam = Camera.open()
            val p: Camera.Parameters = cam.getParameters()
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
            cam.setParameters(p)
            cam.startPreview()*/

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

        arFragment
            ?.arSceneView
            ?.scene
            ?.addOnUpdateListener { frameTime: FrameTime? ->

                Log.e("ANIMATION_ACT", "addOnUpdateListener animators.size : ${animators.size}")

                val time = System.nanoTime()
                for (animator: AnimationInstance in animators) {
                    Log.e("ANIMATION_ACT", "addOnUpdateListener : ${animator.index}, ${((time - animator.startTime) / TimeUnit.SECONDS.toNanos(1).toDouble()).toFloat() % (animator.duration ?: 10f)}")

                    animator.animator!!.applyAnimation(
                        animator.index,
                        ((time - animator.startTime) / TimeUnit.SECONDS.toNanos(1).toDouble())
                            .toFloat() % (animator.duration ?: 10f)
                    )
                    animator.animator!!.updateBoneMatrices()
                }
            }

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
            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(arFragment?.arSceneView?.scene)

            // Create the transformable andy and add it to the anchor.
            TransformableNode(arFragment?.transformationSystem)?.let { mNode ->
                node = mNode
                node?.setParent(anchorNode)
                node?.renderable = currentRenderable?.vrRenderable
                node?.select()

                currentRenderable?.vrObject?.let {
                    vrObjectsMap[it] = mNode
                }

                adapter?.notifyDataSetChanged()

                val filamentAsset = mNode.renderableInstance?.filamentAsset
                val aCount = filamentAsset?.animator?.animationCount ?: 0

                Log.e("ANIMATION_ACT", "aCount : ${aCount}")

                if (aCount > 0) {
                    Log.e("ANIMATION_ACT", "duration : ${filamentAsset?.animator?.getAnimationDuration(0)}")

                    filamentAsset?.animator?.applyAnimation(
                        0, filamentAsset.animator.getAnimationDuration(
                            0
                        )
                    )

                    Log.e("ANIMATION_ACT", "duration : ${filamentAsset?.animator?.getAnimationDuration(0)}")

                    animators.add(
                        AnimationInstance(
                            filamentAsset?.animator,
                            0,
                            System.nanoTime()
                        )
                    )

                    Log.e("ANIMATION_ACT", "duration : ${animators.size}")

                }

                /*val color = colors[nextColor]
                nextColor++
                for (i in 0 until (currentRenderable?.vrRenderable?.getSubmeshCount() ?: 0)) {
                    val material: Material? = currentRenderable?.vrRenderable?.getMaterial(i)
                    material?.setFloat4("baseColorFactor", color)
                }*/
            }
            /*Log.e("ANIMATION_VR", "${currentRenderable?.vrRenderable?.animationDataCount}")
            if ((currentRenderable?.vrRenderable?.animationDataCount ?: 0) > 0) {
                val data: AnimationData? = currentRenderable?.vrRenderable?.getAnimationData(0)
                val animator = ModelAnimator(data, currentRenderable?.vrRenderable)
                animator.start()
            }*/

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

        adapter.addAll(assetsArray)

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }
    }

    open fun isSelectedRenderable(vrObjectDataClass: VrObject): Boolean {
        return currentRenderable?.vrObject?.id == vrObjectDataClass.id
    }

    open fun selectedRenderable(vrObjectDataClass: VrObject): Boolean {
        currentRenderable = cashedAssets[vrObjectDataClass.id] ?: VrRenderableObject(
            vrObject = vrObjectDataClass,
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

    open fun renderableUploaded(vrObjectDataClass: VrObject, renderable: ModelRenderable) {
        flProgressBar.visibility = View.GONE

        VrRenderableObject(
            vrObject = vrObjectDataClass,
            vrRenderable = renderable
        ).let { item ->
            currentRenderable = item
            cashedAssets[vrObjectDataClass.id] = item
        }

        adapter.notifyDataSetChanged()


    }

    open fun renderableUploadedFailed(vrObjectDataClass: VrObject) {
        flProgressBar.visibility = View.GONE
        Toast.makeText(
            this,
            "Unable to load renderable " + vrObjectDataClass.link, Toast.LENGTH_LONG
        ).show()

        adapter.notifyDataSetChanged()
    }
}