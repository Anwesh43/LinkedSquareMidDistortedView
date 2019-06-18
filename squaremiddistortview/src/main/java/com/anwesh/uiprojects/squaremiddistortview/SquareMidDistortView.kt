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
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#0D47A1")
val backColor : Int = Color.parseColor("#BDBDBD")
val offsetFactor : Float = 0.25f
val rotDeg : Float = 90f

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
    drawLine(0f, -rSize, 0f, -offset / 2, paint)
    drawLine(0f, -offset / 2, offset * sc, 0f, paint)
}

fun Canvas.drawMidDistortLine(i : Int, size : Float, sc : Float, paint : Paint) {
    val rSize : Float = size * (1f - offsetFactor)
    val offset : Float = size * offsetFactor
    save()
    rotate(90f * i)
    translate(rSize, 0f)
    for (j in 0..(lines - 1)) {
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
