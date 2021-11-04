package com.example.zrenie20.binakular

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Config.AugmentedFaceMode
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*


/** Implements ArFragment and configures the session for using the augmented faces feature.  */
class BinacularFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        config.focusMode = Config.FocusMode.AUTO
        //config.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR
        return config
    }

    /**
     * Override to turn off planeDiscoveryController. Plane trackables are not supported with the
     * front camera.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val frameLayout =
            super.onCreateView(inflater, container, savedInstanceState) as FrameLayout?
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)

        val config = arSceneView?.session?.cameraConfig

        arSceneView?.session?.cameraConfig = config
        //arSceneView?.isLightDirectionUpdateEnabled = true
        //arSceneView?.isLightEstimationEnabled = true

        return frameLayout
    }
}