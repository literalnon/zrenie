package com.example.zrenie20.common.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.minus
import android.graphics.Bitmap
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import com.google.ar.sceneform.ArSceneView
import kotlin.random.Random


/*class ArSceneBinakularView : ArSceneView {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs,
        0
    ) {
        init(context)
    }

    var mSurfaceView: SurfaceView? = null

    private fun init(context: Context) {
        setWillNotDraw(false)
    }

    var isFirstDraw = true

    override fun draw(canvas: Canvas?) {
        val mHeight = height
        val mWidth = width

        if (isFirstDraw) {
            val nBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            val nCanvas = Canvas(nBitmap)

            val firstChild = children.firstOrNull()
            firstChild!!
                .layoutParams!!
                .height = height / 3
            //(firstChild?.layoutParams as MarginLayoutParams)?.topMargin = height * 2

            mSurfaceView = SurfaceView(context)

            addView(mSurfaceView)

            mSurfaceView?.layoutParams!!.width = width
            mSurfaceView?.layoutParams!!.height = height / 3
            //(mSurfaceView?.layoutParams as MarginLayoutParams)?.topMargin = height * 2

            Log.e("BinacularView", "draw")

            *//*printHierarchy(
                this,
                mSurfaceView
            )*//*
            isFirstDraw = false

        }

        super.draw(canvas)

        *//*val dtr = DrawThreadCanvas(
            mCanvas = canvas!!,
            mBitmap = nBitmap,
            drawCallback = {
                kotlin.run {
                    it()
                }
            }
        )

        dtr.setRunning(true)
        dtr.start()*//*

    }

    *//*override fun onDraw(canvas: Canvas?) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG);
        val width = canvas?.width!!
        val height = canvas?.height!! / 2

        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newBitmap)

        //super.onDraw(newCanvas)

        //canvas?.drawBitmap(newBitmap, 0f, 0f, paint)
    }*//*
    *//*override fun dispatchDraw(canvas: Canvas) {
        val centerHorizontal = measuredWidth.toFloat() / 2
        val radius = measuredWidth.toFloat() / 2

        val centerVertical = measuredHeight.toFloat()

        val path1 = Path()

        path1.arcTo(
            RectF(
                centerHorizontal - radius,
                centerVertical - radius,
                centerHorizontal + radius,
                centerVertical + radius
            ),
            -180f,
            180f,
            false
        )

        val path2 = Path()

        val radius2 = radius / 4

        path2.arcTo(
            RectF(
                centerHorizontal - radius2,
                centerVertical - radius2,
                centerHorizontal + radius2,
                centerVertical + radius2
            ),
            -180f,
            180f,
            false
        )

        val path3 = path1.minus(path2)

        canvas.clipPath(path3)

        super.dispatchDraw(canvas)
    }*//*

    *//*override fun draw(canvas: Canvas?) {
        Log.e("BinacularView", "draw height 1 : ${height}")
        val mWidth = width * 1
        val mHeight = height / 3


        Log.e("BinacularView", "draw height : ${mHeight}")
        var newBitmap1 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas1 = Canvas(newBitmap1)
        val path = Path()

        super.draw(newCanvas1)

        newCanvas1.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        val newBitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas2 = Canvas(newBitmap2)

        super.draw(newCanvas2)

        newCanvas2.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        canvas?.drawBitmap(newBitmap1, 0f, 0f, mPaint)
        canvas?.drawBitmap(newBitmap2, 0f, mHeight * 2f, mPaint)

        super.draw(canvas)
        //canvas?.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)
    }*//*

    var drawThread: DrawThread? = null

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        val isDraw = super.drawChild(canvas, child, drawingTime)

        if (child !is SurfaceView && drawThread == null) {
            drawThread = DrawThread(
                duplicateView = child!!,
                surfaceViewNew = mSurfaceView,
                resources = resources
            )
            drawThread?.setRunning(true)
            drawThread?.start()
        }

        return isDraw
    }

    *//*override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        val mWidth = width * 1
        val mHeight = height / 3

        var newBitmap1 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas1 = Canvas(newBitmap1)

        val isDrawChild1 = super.drawChild(newCanvas1, child, drawingTime)

        val newBitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas2 = Canvas(newBitmap2)

        val isDrawChild2 = super.drawChild(newCanvas2, child, drawingTime)

        canvas?.drawBitmap(newBitmap1, 0f, 0f, mPaint)
        canvas?.drawBitmap(newBitmap2, 0f, mHeight * 2f, mPaint)

        Log.e(
            "BinacularView",
            "drawChild isDrawChild1 : ${isDrawChild1}, isDrawChild2 : ${isDrawChild2}"
        )
        //printHierarchy(child)

        super.drawChild(canvas, child, drawingTime)
        //super.drawChild(canvas, child, drawingTime)

        return isDrawChild1 && isDrawChild2
    }*//*

    fun printHierarchy(
        child: View?,
        surfaceHolderNew: SurfaceView? = null
    ) {
        Log.e("BinacularView", "child : ${child}")

        if (child is ArSceneView) {
            child.session
        }

        if (child is SurfaceView && child != mSurfaceView) {
            //child.drawToBitmap()

            child.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    Log.e("BinacularView", "printHierarchy surfaceChanged")
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.e("BinacularView", "printHierarchy surfaceCreated")
                    *//*drawThread = DrawThread(
                        surfaceViewOld = holder,
                        surfaceViewNew = null,
                        mCanvas = canvas,
                        resources = resources
                    )
                    drawThread?.setRunning(true)
                    drawThread?.start()*//*
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    var retry = true
                    // завершаем работу потока
                    //drawThread?.setRunning(false)

                    while (retry) {
                        try {
                            //drawThread?.join()
                            retry = false
                        } catch (e: InterruptedException) {
                            // если не получилось, то будем пытаться еще и еще
                        }
                    }
                }
            })
        }


        if (child is ViewGroup) {
            child?.children?.forEach {
                printHierarchy(it, surfaceHolderNew)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.e("BinacularView", "onLayout")
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.e("BinacularView", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    override fun childDrawableStateChanged(child: View?) {
        Log.e("BinacularView", "childDrawableStateChanged")
        super.childDrawableStateChanged(child)
    }

    override fun measureChild(
        child: View?,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int
    ) {
        Log.e("BinacularView", "measureChild")
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        Log.e("BinacularView", "onDraw")

        super.onDraw(canvas)
    }

    *//*override fun draw(canvas: Canvas) {
        val mWidth = width
        val mHeight = height

        Log.e("BinacularView", "draw height : ${mHeight}")
        var newBitmap1 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas1 = Canvas(newBitmap1)

        super.draw(canvas)

        //newCanvas1.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        //val newBitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        //val newCanvas2 = Canvas(newBitmap2)

        //super.dispatchDraw(newCanvas2)
        //super.draw(canvas)


        //newCanvas2.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        //canvas?.drawBitmap(newBitmap1, 0f, 0f, mPaint)
        //canvas?.drawBitmap(newBitmap2, 0f, mHeight * 2f, mPaint)
    }*//*
    *//*override fun dispatchDraw(canvas: Canvas?) {
        Log.e("BinacularView", "draw height 1 : ${height}, isWasDraw : ${isWasDraw}")

        val mWidth = width * 1
        val mHeight = height / 3

        Log.e("BinacularView", "draw height : ${mHeight}")
        var newBitmap1 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val newCanvas1 = Canvas(newBitmap1)

        val m1 = newCanvas1.matrix

        Log.e("BinacularView", "draw newCanvas1 : ${newCanvas1.matrix}")

        //super.dispatchDraw(newCanvas1)

        Log.e("BinacularView", "draw newCanvas1 : ${newCanvas1.matrix}, ${m1 == newCanvas1.matrix}")

        //newCanvas1.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        val newBitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        //val newCanvas2 = Canvas(newBitmap2)

        //super.dispatchDraw(newCanvas2)
        canvas?.setBitmap(newBitmap2)
        super.dispatchDraw(canvas)

        //newCanvas2.drawOval(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mPaint!!)

        //canvas?.drawBitmap(newBitmap1, 0f, 0f, mPaint)
        canvas?.drawBitmap(newBitmap2, 0f, mHeight * 2f, mPaint)
    }*//*
}*/
