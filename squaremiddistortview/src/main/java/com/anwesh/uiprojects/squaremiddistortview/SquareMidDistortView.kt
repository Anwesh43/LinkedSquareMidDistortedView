package com.anwesh.uiprojects.squaremiddistortview

/**
 * Created by anweshmishra on 18/06/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val parts : Int = 2
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#0D47A1")
val backColor : Int = Color.parseColor("#BDBDBD")
val offsetFactor : Float = 0.25f
val rotDeg : Float = 90f
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawDistortedLine(offset : Float, rSize : Float, sc : Float, paint : Paint) {
    drawLine(0f, -rSize , 0f, -offset, paint)
    drawLine(0f, -offset, offset * sc, 0f, paint)
}

fun Canvas.drawMidDistortLine(i : Int, size : Float, sc : Float, paint : Paint) {
    val rSize : Float = size * (1f - offsetFactor)
    val offset : Float = size * offsetFactor
    save()
    rotate(90f * i)
    translate(rSize, 0f)
    for (j in 0..(parts - 1)) {
        save()
        scale(1f, 1f - 2 * j)
        drawDistortedLine(offset, rSize, sc.divideScale(i, lines), paint)
        restore()
    }
    restore()
}

fun Canvas.drawSquareMidDistorted(size : Float, sc : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawMidDistortLine(j, size, sc, paint)
    }
}

fun Canvas.drawSMDNode(i : Int, scale : Float, paint : Paint) {
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    save()
    translate(w / 2, gap * (i + 1))
    rotate(rotDeg * sc2)
    drawSquareMidDistorted(size, sc1, paint)
    restore()
}

class SquareMidDistortedView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SMDNode(var i : Int, val state : State = State()) {

        private var next : SMDNode? = null
        private var prev : SMDNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SMDNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSMDNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SMDNode {
            var curr : SMDNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SquareMidDistort(var i : Int) {

        private val root : SMDNode = SMDNode(0)
        private var curr : SMDNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquareMidDistortedView) {

        private val animator : Animator = Animator(view)
        private val smd : SquareMidDistort = SquareMidDistort(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            smd.draw(canvas, paint)
            animator.animate {
                smd.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            smd.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : SquareMidDistortedView {
            val view : SquareMidDistortedView = SquareMidDistortedView(activity)
            activity.setContentView(view)
            return view
        }
    }
}