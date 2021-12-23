package com.example.zrenie20.cloudAnchor2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.example.zrenie20.R
import com.example.zrenie20.myarsample.BaseArActivity
import com.google.ar.sceneform.Scene.OnUpdateListener
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener
import com.google.ar.core.HitResult
import com.google.ar.core.Anchor.CloudAnchorState
import com.example.zrenie20.cloudAnchor2.StorageManager.ShortCodeListener
import com.example.zrenie20.data.DataItemId
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.RenderableCloudId
import com.example.zrenie20.renderable.CustomVisualizer
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.Gson

class MainActivity : BaseArActivity() {
    private var cloudAnchor: Anchor? = null
    override val layoutId: Int
        get() = R.layout.activity_shared2

    private enum class AppAnchorState {
        NONE, HOSTING, HOSTED, RESOLVING, RESOLVED
    }

    private var appAnchorState = AppAnchorState.NONE
    private val snackbarHelper = SnackbarHelper()
    private var storageManager: StorageManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //To add
        arFragment
            ?.getPlaneDiscoveryController()
            ?.hide()

        arFragment
            ?.getArSceneView()
            ?.scene
            ?.addOnUpdateListener { frameTime: FrameTime -> onUpdateFrame(frameTime) }
        //.setOnUpdateListener();
        /*val clearButton = findViewById<Button>(R.id.clear_button)
        clearButton.setOnClickListener { setCloudAnchor(null) }
        val resolveButton = findViewById<Button>(R.id.resolve_button)
        resolveButton.setOnClickListener(View.OnClickListener {
            if (cloudAnchor != null) {
                snackbarHelper.showMessageWithDismiss(parent, "Please clear Anchor")
                return@OnClickListener
            }
            val dialog = ResolveDialogFragment()
            dialog.setOkListener { dialogValue: String -> onResolveOkPressed(dialogValue) }
            dialog.show(supportFragmentManager, "Resolve")
        })*/

        storageManager = StorageManager(this)

        storageManager!!.addSubscriber()

        storageManager!!.getCloudAnchor(object : StorageManager.CloudAnchorIdListener {
            override fun onChildAdded(
                cloudAnchorId: String?,
                itemId: DataItemId?,
                renderableCloudId: RenderableCloudId?
            ) {
                Log.e("MainActivity", "onChildAdded cloudAnchorId : $cloudAnchorId")

                if (vrObjectsMap.keys.find { it == itemId || it == renderableCloudId } != null) {
                    return
                }

                val resolvedAnchor = arFragment
                    ?.getArSceneView()
                    ?.session
                    ?.resolveCloudAnchor(cloudAnchorId)

                setCloudAnchor(resolvedAnchor)
                placeObject(
                    anchor = cloudAnchor,
                    itemId = itemId,
                    renderableCloudId = renderableCloudId
                )
                snackbarHelper.showMessage(this@MainActivity, getString(R.string.resolving_anchor))
                appAnchorState = AppAnchorState.RESOLVING
            }

            override fun onChildChanged(
                cloudAnchorId: String?,
                itemId: DataItemId?,
                renderableId: RenderableCloudId?
            ) {
                Log.e("MainActivity", "onChildChanged cloudAnchorId : $cloudAnchorId")

            }

            override fun onChildRemoved(
                cloudAnchorId: String?,
                itemId: DataItemId?,
                renderableCloudId: RenderableCloudId?
            ) {
                Log.e("MainActivity", "onChildRemoved cloudAnchorId : $cloudAnchorId")
                val obj = assetsArray.find { it.id == itemId }

                renderableRemove(
                    dataItemObject = obj,
                    renderableCloudId = renderableCloudId
                )
            }
        })
    }

    override fun onTapArPlane(hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent?) {
        Log.e(
            "MainActivity",
            "onTapArPlane ${plane?.type != Plane.Type.HORIZONTAL_UPWARD_FACING}, ${appAnchorState != AppAnchorState.NONE}, ${currentRenderable == null}"
        )

        /*
        plane?.type != Plane.Type.HORIZONTAL_UPWARD_FACING ||
                appAnchorState != AppAnchorState.NONE ||
                */
        if (currentRenderable == null
        ) {
            return
        }

        val newAnchor = arFragment!!.arSceneView
            .session
            ?.hostCloudAnchor(hitResult.createAnchor())

        setCloudAnchor(newAnchor)
        appAnchorState = AppAnchorState.HOSTING
        snackbarHelper.showMessage(this, getString(R.string.hosting_anchor))
        //placeObject(cloudAnchor, currentRenderable!!.dataItemObject.id)
    }

    /*private fun onResolveOkPressed(dialogValue: String) {
        val shortCode = dialogValue.toInt()
        storageManager!!.getCloudAnchorID(
            shortCode,
            object : StorageManager.CloudAnchorIdListener {
                override fun onCloudAnchorIdAvailable(cloudAnchorId: String?, itemId: DataItemId?) {
                Log.e(
                    "CloudAnchors",
                    "onResolveOkPressed cloudAnchorId : $cloudAnchorId shortCode : $shortCode"
                )
                val resolvedAnchor = arFragment
                    ?.getArSceneView()
                    ?.session
                    ?.resolveCloudAnchor(cloudAnchorId)

                setCloudAnchor(resolvedAnchor)
                placeObject(cloudAnchor, null)
                snackbarHelper.showMessage(this@MainActivity, "Now Resolving Anchor...")
                appAnchorState = AppAnchorState.RESOLVING
            }})
    }*/

    private fun setCloudAnchor(newAnchor: Anchor?) {
        Log.e("MainActivity", "setCloudAnchor")
        /*if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }*/
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
        snackbarHelper.hide(this)
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        //Log.e("MainActivity", "onUpdateFrame")
        checkUpdatedAnchor()
    }

    @Synchronized
    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor!!.cloudAnchorState
        if (appAnchorState == AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                Log.e("MainActivity", "checkUpdatedAnchor error hosting cloudState : ${cloudState}")
                snackbarHelper.showMessageWithDismiss(
                    this, ""
                            + cloudState
                )
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == CloudAnchorState.SUCCESS) {
                storageManager!!.nextShortCode(object : ShortCodeListener {
                    override fun onShortCodeAvailable(shortCode: Int?) {
                        if (shortCode == null) {
                            snackbarHelper.showMessageWithDismiss(
                                this@MainActivity,
                                "Could not get shortCode"
                            )
                            return
                        }
                        storageManager!!.storeUsingShortCodeWithId(
                            shortCode,
                            cloudAnchor!!.cloudAnchorId,
                            currentRenderable!!.dataItemObject.id.toString()
                        )
                        snackbarHelper.showMessageWithDismiss(
                            this@MainActivity, "Anchor hosted! Cloud Short Code: " +
                                    shortCode
                        )
                    }
                })
                appAnchorState = AppAnchorState.HOSTED
            }
        } else if (appAnchorState == AppAnchorState.RESOLVING) {
            if (cloudState.isError) {
                Log.e(
                    "MainActivity",
                    "checkUpdatedAnchor error resolving cloudState : ${cloudState}"
                )
                snackbarHelper.showMessageWithDismiss(
                    this, "Error resolving anchor.. "
                            + cloudState
                )
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == CloudAnchorState.SUCCESS) {
                snackbarHelper.showMessageWithDismiss(this, getString(R.string.hosting_anchor))
                appAnchorState = AppAnchorState.RESOLVED
            }
        }
    }

    fun showCurrentRenderable(anchor: Anchor?, renderableCloudId: RenderableCloudId?) {
        Log.e("MainActivity", "showCurrentRenderable : ${renderableCloudId}")

        if (renderableCloudId == null) {
            return
        }

        anchorNode = AnchorNode(anchor)

        anchorNode?.setParent(arFragment?.arSceneView?.scene)
        val scale = currentRenderable?.dataItemObject?.scale?.toFloatOrNull() ?: 4f

        anchorNode?.localScale = Vector3(scale, scale, scale)

        //val renderable = currentRenderable?.getRenderable() //?: return

        currentRenderable?.start(
            anchor = anchor,
            onSuccess = {
                currentRenderable?.getRenderable()?.let { renderable ->

                    TransformableNode(arFragment?.transformationSystem)?.let { mNode ->

                        mNode.transformationSystem?.selectionVisualizer = CustomVisualizer()

                        mNode.scaleController.minScale = BASE_MIN_SCALE//0.01f//Float.MIN_VALUE
                        mNode.scaleController.maxScale = BASE_MAX_SCALE//5f//Float.MAX_VALUE

                        node = mNode
                        node?.setParent(anchorNode)

                        Log.e(
                            "MainActivity",
                            "thread : ${Thread.currentThread().name} showCurrentRenderable renderableCloudId : ${renderableCloudId}, currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}, ${renderable != null}, ${currentRenderable != null}, ${sceneView?.scene?.view?.renderer != null}, ${arFragment != null}"
                        )

                        node?.renderable = renderable
                        node?.select()

                        Log.e(
                            "MainActivity",
                            "showCurrentRenderable renderableCloudId : ${renderableCloudId}, currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}"
                        )
                        vrObjectsMap[renderableCloudId] = Pair(currentRenderable?.dataItemObject?.id!!, mNode)

                        adapter?.notifyDataSetChanged()
                    }
                }
            },
            onFailure = {
                if (currentRenderable != null && isSelectedRenderable(currentRenderable!!.dataItemObject)) {
                    renderableUploadedFailed(currentRenderable!!.dataItemObject)
                }
            }
        )
    }

    override fun renderableRemove(
        dataItemObject: DataItemObject?,
        renderableCloudId: RenderableCloudId?
    ) {
        Log.e(
            "MainActivity",
            "renderableRemove renderableCloudId : ${renderableCloudId}, dataItemObject?.id : ${dataItemObject?.id} : ${Gson().toJson(vrObjectsMap.keys)}"
        )
        Log.e(
            "MainActivity",
            "renderableRemove ${Gson().toJson(vrObjectsMap.keys)}"
        )
        Log.e(
            "MainActivity",
            "renderableRemove ${Gson().toJson(vrObjectsMap.values.map { it.first })}"
        )
        super.renderableRemove(dataItemObject, renderableCloudId)

        val rCloudId = renderableCloudId ?: vrObjectsMap.filter {
            it.value.first == dataItemObject?.id
        }.keys.firstOrNull()

        storageManager?.removeChild(rCloudId, dataItemObject?.id!!)
    }

    private fun placeObject(
        anchor: Anchor?,
        renderableCloudId: RenderableCloudId?,
        itemId: DataItemId?
    ) {
        Log.e(
            "MainActivity",
            "placeObject renderableCloudId: ${renderableCloudId}, itemId : ${itemId},  currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}"
        )

        if (itemId != null && currentRenderable?.dataItemObject?.id != itemId) {
            if (cashedAssets[renderableCloudId] != null) {
                currentRenderable = cashedAssets[renderableCloudId]
            } else {
                loadRenderableById(itemId, anchor, renderableCloudId)
                return
            }
        }

        showCurrentRenderable(anchor, renderableCloudId)
    }

    override fun positionRenderableOnPlane(anchor: Anchor?, renderableCloudId: RenderableCloudId?) {
        if (renderableCloudId == null) {
            return
        }

        anchorNode = AnchorNode(anchor)
        Log.e("renderable", "anchorNode.worldPosition : ${anchorNode?.worldPosition}, renderableCloudId : ${renderableCloudId}")

        anchorNode?.setParent(arFragment?.arSceneView?.scene)
        val scale = currentRenderable?.dataItemObject?.scale?.toFloatOrNull() ?: 4f

        anchorNode?.localScale = Vector3(scale, scale, scale)

        currentRenderable?.start(
            anchor = anchor,
            onSuccess = {
                currentRenderable?.getRenderable()?.let { renderable ->
                    TransformableNode(arFragment?.transformationSystem)?.let { mNode ->

                        mNode.transformationSystem?.selectionVisualizer = CustomVisualizer()

                        mNode.scaleController.minScale = BASE_MIN_SCALE//0.01f//Float.MIN_VALUE
                        mNode.scaleController.maxScale = BASE_MAX_SCALE//5f//Float.MAX_VALUE

                        node = mNode
                        node?.setParent(anchorNode)

                        Log.e(
                            "MainActivity",
                            "thread : ${Thread.currentThread().name} positionRenderableOnPlane renderableCloudId : ${renderableCloudId}, currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}, ${renderable != null}, ${currentRenderable != null}, ${sceneView?.scene?.view?.renderer != null}, ${arFragment != null}"
                        )

                        node?.renderable = renderable
                        node?.select()

                        Log.e(
                            "MainActivity",
                            "positionRenderableOnPlane renderableCloudId : ${renderableCloudId}, currentRenderable?.dataItemObject?.id : ${currentRenderable?.dataItemObject?.id}"
                        )

                        vrObjectsMap[renderableCloudId] =
                            Pair(currentRenderable?.dataItemObject?.id!!, mNode)

                        adapter?.notifyDataSetChanged()
                    }
                }
            },
            onFailure = {
                if (currentRenderable != null && isSelectedRenderable(currentRenderable!!.dataItemObject)) {
                    renderableUploadedFailed(currentRenderable!!.dataItemObject)
                }
            }
        )
    }

    override fun onDestroy() {
        storageManager?.removeSubscriber()
        super.onDestroy()
    }

}