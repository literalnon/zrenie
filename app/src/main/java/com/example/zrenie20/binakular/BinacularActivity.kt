package com.example.zrenie20.binakular

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.TextureView
import com.alphamovie.lib.AlphaMovieView
import com.example.zrenie20.R
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.renderable.alpha.MAlphaMovieView
import kotlinx.android.synthetic.main.activity_binacular.*

open class BinacularActivity : BaseArActivity() {
    override val layoutId: Int = R.layout.activity_binacular

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("BinacularActivity", "arFragment?.arSceneView?.session : ${arFragment?.arSceneView?.session}")

        Handler().postDelayed({
            arFragment?.arSceneView?.arFrame
            //mArSceneView?.set
            mArSceneView?.setupSession(arFragment?.arSceneView?.session)
        }, 2000)

        val textureView = findViewById<TextureView>(R.id.videoPlayer)
        //webView?.loadUrl("http://developer.alexanderklimov.ru/android/views/webview.php");
        //videoPlayer?.setVideoFromAssets("ball.mp4")//VideoSurfaceView
        val player = MAlphaMovieView(textureView)
        player.setVideoFromAssets("ball.mp4")
    }
}