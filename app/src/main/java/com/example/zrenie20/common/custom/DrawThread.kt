package com.example.zrenie20.common.custom

import android.content.res.Resources
import android.graphics.*
import android.util.Log

import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.applyCanvas
import androidx.core.view.drawToBitmap
import kotlin.random.Random

class DrawThread(
    private val duplicateView: View,
    private val surfaceViewNew: SurfaceView?,
    private val resources: Resources?
) : Thread() {
    private var runFlag = false

    private var prevTime: Long
    fun setRunning(run: Boolean) {
        Log.e("BinacularView", "DrawThread setRunning")
        runFlag = run
    }

    override fun run() {
        //Log.e("BinacularView", "DrawThread run runFlag : ${runFlag}")
        var lastSeen = System.currentTimeMillis()
        var lastBitmapSeen = System.currentTimeMillis()
        val mHeight = surfaceViewNew?.height!!
        val mWidth = surfaceViewNew?.width!!

        var nBitmap: Bitmap? = null//Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        var canvas: Canvas? = null

        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = Color.WHITE

        loop@ while (runFlag) {

            val curSeen = System.currentTimeMillis()

            if (curSeen - lastSeen < 1000) {
                continue@loop
            }

            //lastBitmapSeen = System.currentTimeMillis()

            val newHolder = surfaceViewNew?.holder!!

            try {
                canvas = newHolder.lockCanvas()

                synchronized(newHolder) {
                    Log.e("BinacularView", "DrawThread mHeight : ${mHeight}")

                    if (curSeen - lastSeen < 10000) {

                        nBitmap = duplicateView?.drawToBitmap()

                        lastSeen = System.currentTimeMillis()
                    }

                    //val r = Random.nextInt() % 1000

                    //nCanvas?.drawOval(r + 0f, r + 0f, r + 100f, r + 100f, mPaint)
                    nBitmap?.let { nBitmap ->
                        Log.e("BinacularView", "DrawThread nBitmap != null")

                        canvas?.drawBitmap(
                            nBitmap,
                            0f,
                            0f,
                            mPaint
                        )
                    }

                    //canvas?.drawOval(r + 0f, r + 0f, r + 100f, r + 100f, mPaint)
                    /*nBitmap?.let {
                    canvas?.drawBitmap(
                        nBitmap,
                        0f,
                        0f,
                        mPaint
                    )
                }*/
                    //mCanvas?.drawOval(0f, mHeight * 2f, mCanvas?.width?.toFloat() ?: 1000f, mHeight * 3f, mPaint)
                }

                /*var newBitmap1 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            val newCanvas1 = Canvas(newBitmap1)

            newCanvas1.drawOval(0f, mHeight * 2f, mWidth.toFloat(), mHeight * 3f, mPaint!!)

            //surfaceViewOld.drawToBitmap()
            mCanvas?.drawBitmap(
                newBitmap1,
                0f,
                mHeight * 2f,
                mPaint
            )*/
            } finally {
                if (canvas != null) {
                    // отрисовка выполнена. выводим результат на экран
                    newHolder.unlockCanvasAndPost(canvas)
                    canvas = null
                }
            }
        }
        //}
    }

    init {
        Log.e("BinacularView", "DrawThread init")
        // загружаем картинку, которую будем отрисовывать
        //picture = BitmapFactory.decodeResource(resources, R.drawable.splash)

        // сохраняем текущее время
        prevTime = System.currentTimeMillis()
    }
}