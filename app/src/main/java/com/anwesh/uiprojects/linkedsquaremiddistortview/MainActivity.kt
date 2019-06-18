package com.anwesh.uiprojects.linkedsquaremiddistortview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.anwesh.uiprojects.squaremiddistortview.SquareMidDistortedView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquareMidDistortedView.create(this)
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}