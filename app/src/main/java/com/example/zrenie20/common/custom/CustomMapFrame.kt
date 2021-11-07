package com.example.zrenie20.common.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.core.graphics.minus


class RoundCornerLayout : FrameLayout {
    private var CORNER_RADIUS = 150f
    private var mCornerRadius = 0f
    private var mPaint: Paint? = null
    private var mMaskPaint: Paint? = null
    private var mMetrics: DisplayMetrics? = null

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

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mMetrics = context.resources.displayMetrics
        mCornerRadius =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CORNER_RADIUS, mMetrics)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        mMaskPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setWillNotDraw(false)
    }

    override fun dispatchDraw(canvas: Canvas) {
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
    }
}
