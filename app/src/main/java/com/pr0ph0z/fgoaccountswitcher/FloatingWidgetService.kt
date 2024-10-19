package com.pr0ph0z.fgoaccountswitcher

import AccountAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.annotation.RequiresApi
import com.pr0ph0z.fgoaccountswitcher.util.RootFileAccess


class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var floatingBoxView: View
    private lateinit var closeAreaView: View
    private var layoutParams: WindowManager.LayoutParams? = null
    private var floatingBoxLayoutParams: WindowManager.LayoutParams? = null
    private var closeAreaLayoutParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var isBoxVisible = false

    private var accountList: ArrayList<Account> = arrayListOf()
    private lateinit var adapter: AccountAdapter
    private lateinit var listView: ListView

    private var rootFileAccess = RootFileAccess()
    private var accountManager = AccountManager(rootFileAccess)

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null)
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // For overlaying other apps
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,        // Do not receive input events unless tapped
            PixelFormat.TRANSLUCENT                               // Transparent background
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        windowManager.addView(floatingView, layoutParams)

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

        closeAreaView = LayoutInflater.from(this).inflate(R.layout.close_area, null)
        closeAreaLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            200,  // Adjust to match close area height
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        windowManager.addView(closeAreaView, closeAreaLayoutParams)


        val closeIcon = closeAreaView.findViewById<ImageView>(R.id.ca_close)

        val menuButton = floatingView.findViewById<ImageView>(R.id.menu_button)

        val closeBoxButton = floatingBoxView.findViewById<Button>(R.id.close_box_button)
        closeBoxButton.setOnClickListener {
            windowManager.removeView(floatingBoxView)
            windowManager.addView(floatingView, layoutParams)
            isBoxVisible = false
        }

        val dragThreshold = 10
        var isDragging = false

        menuButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    closeAreaView.visibility = View.VISIBLE

                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (motionEvent.rawX - initialTouchX).toInt()
                    val deltaY = (motionEvent.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > dragThreshold || Math.abs(deltaY) > dragThreshold) {
                        isDragging = true
                        layoutParams!!.x = initialX + deltaX
                        layoutParams!!.y = initialY + deltaY

                        val screenHeight = resources.displayMetrics.heightPixels

                        // Get the button's current position
                        val buttonCenterY = layoutParams!!.y + floatingView.height / 2
                        val buttonCenterX = layoutParams!!.x + floatingView.width / 2

                        // Get the close icon's center position
                        val closeIconCenterY = screenHeight - closeAreaView.height / 2
                        val closeIconCenterX = (resources.displayMetrics.widthPixels / 2)

                        // Check if the button is within the "magnetic" range
                        val magnetRange = 150 // Distance in pixels to trigger the magnet effect
                        val distanceToCloseArea = Math.sqrt(
                            Math.pow((buttonCenterX - closeIconCenterX).toDouble(), 2.0) +
                                    Math.pow((buttonCenterY - closeIconCenterY).toDouble(), 2.0)
                        )

                        if (distanceToCloseArea <= magnetRange) {
                            // Animate the button to the center of the close icon
                            layoutParams!!.x = closeIconCenterX - floatingView.width / 2
                            layoutParams!!.y = closeIconCenterY - floatingView.height / 2
                            windowManager.updateViewLayout(floatingView, layoutParams)
                            val dimensionInDp = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                70F,
                                resources.displayMetrics
                            ).toInt()
                            closeIcon.layoutParams.width = dimensionInDp
                            closeIcon.layoutParams.height = dimensionInDp
                            closeIcon.requestLayout()
                            closeIcon.setColorFilter(Color.RED)
                        } else {
                            val dimensionInDp = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                60F,
                                resources.displayMetrics
                            ).toInt()
                            closeIcon.layoutParams.width = dimensionInDp
                            closeIcon.layoutParams.height = dimensionInDp
                            closeIcon.requestLayout()
                            closeIcon.setColorFilter(Color.WHITE)
                        }

                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        menuButton.performClick()
                    } else {
                        val screenHeight = resources.displayMetrics.heightPixels
                        val closeAreaTop = screenHeight - closeAreaView.height

                        val closeIconCenterY = (screenHeight - closeAreaView.height / 2) - floatingView.height / 2
                        var closeIconCenterX = (resources.displayMetrics.widthPixels / 2) - floatingView.width / 2

                        // If the button is released near the close area, stop the service
                        if (layoutParams!!.x == closeIconCenterX && layoutParams!!.y == closeIconCenterY) {
                            stopSelf()  // Stop the service
                        } else {
                            val screenWidth = resources.displayMetrics.widthPixels
                            val middleX = screenWidth / 2

                            val targetX = if (layoutParams!!.x >= middleX) {
                                screenWidth - floatingView.width
                            } else {
                                0
                            }

                            val animator = ValueAnimator.ofInt(layoutParams!!.x, targetX)
                            animator.addUpdateListener { animation ->
                                layoutParams!!.x = animation.animatedValue as Int
                                windowManager.updateViewLayout(floatingView, layoutParams)
                            }
                            animator.duration = 300
                            animator.start()
                        }
                    }
                    closeAreaView.visibility = View.GONE
                    true
                }
                else -> false
            }
        }

        menuButton.setOnClickListener {
            if (isBoxVisible) {
                windowManager.removeView(floatingBoxView)
                windowManager.addView(floatingView, layoutParams)
                isBoxVisible = false
            } else {
                windowManager.removeView(floatingView)
                floatingBoxLayoutParams!!.x = layoutParams!!.x
                floatingBoxLayoutParams!!.y = layoutParams!!.y
                windowManager.addView(floatingBoxView, floatingBoxLayoutParams)
                isBoxVisible = true
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::listView.isInitialized) {
            listView = floatingBoxView.findViewById(R.id.lv_accounts)
            adapter = AccountAdapter(applicationContext, R.layout.listview_row, accountList)
            listView.adapter = adapter
            listView.setOnItemClickListener { parent, view, position, id ->
                val selectedAccount = accountList[position]
                accountManager.switchAccount(applicationContext, selectedAccount)
            }
        }

        val accounts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra("accounts", Account::class.java)
        } else {
            intent?.getParcelableArrayListExtra("accounts")
        }

        accounts?.let {
            accountList.clear()
            accountList.addAll(it)

            adapter.notifyDataSetChanged()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
        if (::closeAreaView.isInitialized) windowManager.removeView(closeAreaView)
    }
}
