package com.bmdelacruz.vgp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class ThumbStickHandle(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), View.OnTouchListener {
    private var originX = 0f
    private var originY = 0f

    lateinit var onPositionChanged: (x: Float, y: Float) -> Unit
    lateinit var onPositionReset: () -> Unit

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                originX = v.x
                originY = v.y
                val tX = ((event.x / v.width) * 2) - 1
                val tY = ((event.y / v.height) * 2) - 1
                translationX = tX.toFloat() * v.width / 2
                translationY = tY.toFloat() * v.height / 2

                true
            }
            MotionEvent.ACTION_UP -> {
                translationX = 0f
                translationY = 0f

                onPositionReset()

                invalidate()

                true
            }
            MotionEvent.ACTION_MOVE -> {
                var newTx = ((((event.rawX - originX) / v.width) * 2) - 1) * v.width / 2
                var newTy = ((((event.rawY - originY) / v.height) * 2) - 1) * v.height / 2

                val distanceSquared = newTx * newTx + newTy * newTy
                if (distanceSquared >= DISTANCE_THRESHOLD_SQUARED) {
                    if (distanceSquared >= MAX_DISTANCE_SQUARED) {
                        val magnitude = sqrt(distanceSquared)
                        val utx = newTx / magnitude
                        val uty = newTy / magnitude

                        newTx = utx * MAX_DISTANCE
                        newTy = uty * MAX_DISTANCE

                        onPositionChanged(utx, uty)
                    } else {
                        val utx = newTx / MAX_DISTANCE
                        val uty = newTy / MAX_DISTANCE

                        onPositionChanged(utx, uty)
                    }
                }

                translationX = newTx
                translationY = newTy

                invalidate()

                true
            }
            else -> false
        }
    }

    companion object {
        private const val MAX_DISTANCE = 60f
        private val MAX_DISTANCE_SQUARED = MAX_DISTANCE.pow(2)
        private val DISTANCE_THRESHOLD_SQUARED = 5f.pow(2)
    }
}
