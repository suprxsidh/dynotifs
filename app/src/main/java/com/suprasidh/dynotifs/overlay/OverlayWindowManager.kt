package com.suprasidh.dynotifs.overlay

import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.domain.model.IslandState
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.ui.components.IslandExpanded
import com.suprasidh.dynotifs.ui.components.IslandPill
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayWindowManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DynotifsDataStore
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private var pillView: ComposeView? = null
    private var expandedView: ComposeView? = null
    private var isShowingPill = false
    private var isShowingExpanded = false

    private val _currentState = MutableStateFlow(IslandState.Hidden)
    val currentState: StateFlow<IslandState> = _currentState.asStateFlow()

    private var currentItem: NotificationItem? = null

    fun getContext(): Context = context

    private fun getScreenMetrics(): Point {
        val display = wm.defaultDisplay
        val point = Point()
        display.getRealSize(point)
        return point
    }

    private fun getPillLayoutParams(): WindowManager.LayoutParams {
        val metrics = getScreenMetrics()

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun getExpandedLayoutParams(): WindowManager.LayoutParams {
        val metrics = getScreenMetrics()
        val defaultWidth = (metrics.x * 0.35).toInt()
        val defaultHeight = (metrics.y * 0.25).toInt()

        return WindowManager.LayoutParams(
            defaultWidth,
            defaultHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    fun showPill(item: NotificationItem) {
        try {
            hideExpandedInternal()
            currentItem = item

            if (pillView == null) {
                pillView = ComposeView(context).apply {
                    setContent {
                        IslandPill(
                            title = item.title,
                            text = item.text,
                            icon = item.icon,
                            onTap = { onPillTapped() },
                            onLongPress = { onPillLongPressed() },
                            onHorizontalDrag = { onPillHorizontalDrag() }
                        )
                    }
                }
            }

            val lp = getPillLayoutParams()
            wm.addView(pillView, lp)
            isShowingPill = true
            _currentState.value = IslandState.Pill
        } catch (e: BadTokenException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun showExpanded(item: NotificationItem) {
        try {
            hidePillInternal()
            currentItem = item

            if (expandedView == null) {
                expandedView = ComposeView(context).apply {
                    setContent {
                        IslandExpanded(
                            item = item,
                            onCollapse = { onExpandedCollapsed() },
                            onSendReply = { action -> onExpandedSendReply(action) }
                        )
                    }
                }
            }

            val lp = getExpandedLayoutParams()
            lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            wm.addView(expandedView, lp)
            isShowingExpanded = true
            _currentState.value = IslandState.Expanded
        } catch (e: BadTokenException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun hide() {
        hidePillInternal()
        hideExpandedInternal()
        _currentState.value = IslandState.Hidden
        currentItem = null
    }

    private fun hidePillInternal() {
        if (isShowingPill) {
            try {
                pillView?.let { wm.removeViewImmediate(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            pillView = null
            isShowingPill = false
        }
    }

    private fun hideExpandedInternal() {
        if (isShowingExpanded) {
            try {
                expandedView?.let {
                    var params = it.layoutParams as? WindowManager.LayoutParams
                    if (params != null) {
                        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        wm.updateViewLayout(it, params)
                    }
                }
                wm.removeViewImmediate(expandedView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            expandedView = null
            isShowingExpanded = false
        }
    }

    private fun onPillTapped() {
        currentItem?.contentIntent?.send()
    }

    private fun onPillLongPressed() {
        currentItem?.let { showExpanded(it) }
    }

    private fun onPillHorizontalDrag() {
        currentItem?.let { item ->
            // This will trigger cancellation
        }
    }

    private fun onExpandedCollapsed() {
        hideExpandedInternal()
        val item = currentItem
        if (item != null) {
            showPill(item)
        }
    }

    private fun onExpandedSendReply(action: String) {
        // Handle RemoteInput reply
    }
}