package com.suprasidh.dynotifs.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.domain.model.IslandState
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.ui.components.IslandPill
import com.suprasidh.dynotifs.ui.components.IslandExpanded
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
    private val context: Context,
    private val dataStore: DynotifsDataStore
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(Dispatchers.Main)

    private var pillView: ComposeView? = null
    private var expandedView: ComposeView? = null
    private var isShowingPill = false
    private var isShowingExpanded = false

    private val _currentState = MutableStateFlow(IslandState.HIDDEN)
    val currentState: StateFlow<IslandState> = _currentState.asStateFlow()

    var onPillTapped: (() -> Unit)? = null
    var onPillLongPressed: (() -> Unit)? = null
    var onPillDragged: (() -> Unit)? = null

    private fun getScreenMetrics(): Point {
        val display = wm.defaultDisplay
        val point = Point()
        display.getRealSize(point)
        return point
    }

    private fun isLandscape(): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun showPill(item: NotificationItem) {
        if (isLandscape()) return

        try {
            hideExpanded()
            pillView = ComposeView(context).apply {
                setContent {
                    IslandPill(
                        title = item.title,
                        text = item.text,
                        icon = item.icon,
                        onTap = { onPillTapped?.invoke() },
                        onLongPress = { onPillLongPressed?.invoke() },
                        onDrag = { onPillDragged?.invoke() }
                    )
                }
            }
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN),
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            wm.addView(pillView, lp)
            isShowingPill = true
            _currentState.value = IslandState.PILL
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun showExpanded(item: NotificationItem) {
        if (isLandscape()) return

        try {
            hidePill()
            expandedView = ComposeView(context).apply {
                setContent {
                    IslandExpanded(
                        item = item,
                        onCollapse = { hideExpanded(); showPill(item) },
                        onSendReply = { /* Handle reply */ }
                    )
                }
            }
            val metrics = getScreenMetrics()
            val lp = WindowManager.LayoutParams(
                (metrics.x * 0.35).toInt(),
                (metrics.y * 0.25).toInt(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN),
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
            lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            wm.addView(expandedView, lp)
            isShowingExpanded = true
            _currentState.value = IslandState.EXPANDED
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun hide() {
        hidePill()
        hideExpanded()
        _currentState.value = IslandState.HIDDEN
    }

    private fun hidePill() {
        if (isShowingPill) {
            try { pillView?.let { wm.removeViewImmediate(it) } } catch (e: Exception) { }
            pillView = null
            isShowingPill = false
        }
    }

    private fun hideExpanded() {
        if (isShowingExpanded) {
            try {
                expandedView?.let { wm.removeViewImmediate(it) }
            } catch (e: Exception) { }
            expandedView = null
            isShowingExpanded = false
        }
    }
}