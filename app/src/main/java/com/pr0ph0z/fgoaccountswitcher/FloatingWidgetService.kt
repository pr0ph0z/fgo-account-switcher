package com.pr0ph0z.fgoaccountswitcher

import AccountAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
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
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pr0ph0z.fgoaccountswitcher.util.RootFileAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        menuButton.performClick()
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
    }
}
