package com.pr0ph0z.fgoaccountswitcher

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.Image
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.button.MaterialButton

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var floatingBoxView: View
    private var layoutParams: WindowManager.LayoutParams? = null
    private var floatingBoxLayoutParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var isBoxVisible = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the floating widget layout
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null)
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // For overlaying other apps
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,        // Do not receive input events unless tapped
            PixelFormat.TRANSLUCENT                               // Transparent background
        ).apply {
            gravity = Gravity.TOP or Gravity.START  // Starting position at the top-left corner
            x = 0
            y = 100
        }

        floatingBoxView = LayoutInflater.from(this).inflate(R.layout.floating_box, null)
        floatingBoxLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager.addView(floatingView, layoutParams)

        // Set up the close button
        val closeButton = floatingView.findViewById<ImageView>(R.id.close_button)
        closeButton.setOnClickListener {
            if (isBoxVisible) {
                // Minimize the box (hide the box and show the button)
                windowManager.removeView(floatingBoxView)
                windowManager.addView(floatingView, layoutParams)
                isBoxVisible = false
            } else {
                // Show the floating box (hide the button and show the box)
                windowManager.removeView(floatingView)
                floatingBoxLayoutParams!!.x = layoutParams!!.x
                floatingBoxLayoutParams!!.y = layoutParams!!.y
                windowManager.addView(floatingBoxView, floatingBoxLayoutParams)
                isBoxVisible = true
            }
        }

        // Set up the close button
        val infoButton = floatingView.findViewById<ImageView>(R.id.info_button)
        infoButton.setOnClickListener {
            if (isBoxVisible) {
                // Minimize the box (hide the box and show the button)
                windowManager.removeView(floatingBoxView)
                windowManager.addView(floatingView, layoutParams)
                isBoxVisible = false
            } else {
                // Show the floating box (hide the button and show the box)
                windowManager.removeView(floatingView)
                windowManager.addView(floatingBoxView, floatingBoxLayoutParams)
                isBoxVisible = true
            }
        }

        val closeBoxButton = floatingBoxView.findViewById<ImageView>(R.id.close_box_button)
        closeBoxButton.setOnClickListener {
            windowManager.removeView(floatingBoxView)
            windowManager.addView(floatingView, layoutParams)
            isBoxVisible = false
        }

        // Make the floating widget draggable
        infoButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Initial touch logic (same as before)
                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Move logic (same as before)
                    layoutParams!!.x = initialX + (motionEvent.rawX - initialTouchX).toInt()
                    layoutParams!!.y = initialY + (motionEvent.rawY - initialTouchY).toInt()

                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Get the screen width
                    val screenWidth = resources.displayMetrics.widthPixels
                    val middleX = screenWidth / 2

                    // Determine target position (left or right side)
                    val targetX = if (layoutParams!!.x >= middleX) {
                        screenWidth - floatingView.width
                    } else {
                        0
                    }

                    // Animate the floating button snapping to the side
                    val animator = ValueAnimator.ofInt(layoutParams!!.x, targetX)
                    animator.addUpdateListener { animation ->
                        layoutParams!!.x = animation.animatedValue as Int
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    animator.duration = 300 // Animation duration in milliseconds
                    animator.start()
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    private fun showListPopup() {
        // Show your list in a popup window or activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}
