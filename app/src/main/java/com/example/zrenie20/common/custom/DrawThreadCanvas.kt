package com.example.zrenie20.common.custom

import android.content.res.Resources
import android.graphics.*
import android.util.Log

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.applyCanvas
import androidx.core.view.drawToBitmap
import kotlin.random.Random

class DrawThreadCanvas(
    val mCanvas: Canvas,
    val mBitmap: Bitmap,
    val drawCallback: (() -> Unit) -> Unit
) : Thread() {
    private var runFlag = false

    fun setRunning(run: Boolean) {
        Log.e("BinacularView", "DrawThread setRunning")
        runFlag = run
    }

    override fun run() {
        Log.e("BinacularView", "DrawThread run runFlag : ${runFlag}")
        var canvas: Canvas?
        var lastSeen = System.currentTimeMillis()
        var lastBitmapSeen = System.currentTimeMillis()

        var nBitmap: Bitmap? = null
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        loop@ while (runFlag) {
            val curSeen = System.currentTimeMillis()

            if (curSeen - lastSeen < 100) {
                continue@loop
            }

            Log.e("BinacularView", "DrawThread run w runFlag : ${runFlag}")

            lastSeen = System.currentTimeMillis()

            val r = Random.nextInt() % 1000

            drawCallback {
                /*mCanvas?.drawBitmap(
                    mBitmap,
                    0f,
                    0f,
                    mPaint
                )*/
                mCanvas.drawOval(r + 10f, r + 10f, r + 100f, r + 100f, mPaint)
            }
        }
    }

    init {
        Log.e("BinacularView", "DrawThread init")
    }
}