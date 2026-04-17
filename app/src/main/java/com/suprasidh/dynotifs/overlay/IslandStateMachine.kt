package com.suprasidh.dynotifs.overlay

import android.content.res.Configuration
import android.view.WindowManager
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.domain.model.IslandState
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.queue.PriorityNotificationQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IslandStateMachine @Inject constructor(
    private val queue: PriorityNotificationQueue,
    private val dataStore: DynotifsDataStore,
    private val overlayWindowManager: OverlayWindowManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _state = MutableStateFlow<IslandState>(IslandState.Hidden)
    val state: StateFlow<IslandState> = _state.asStateFlow()

    private val _currentItem = MutableStateFlow<NotificationItem?>(null)
    val currentItem: StateFlow<NotificationItem?> = _currentItem.asStateFlow()

    private val _islandEnabled = MutableStateFlow(true)
    val islandEnabled: StateFlow<Boolean> = _islandEnabled.asStateFlow()

    private var collapseTimerJob: Job? = null
    private var pendingKey: String? = null

    companion object {
        private const val COLLAPSE_DELAY_MS = 5000L
    }

    init {
        scope.launch {
            dataStore.islandEnabledFlow.collect { enabled ->
                _islandEnabled.value = enabled
                if (!enabled) {
                    hideIsland()
                }
            }
        }
    }

    fun onNotificationPosted(item: NotificationItem) {
        if (!checkOrientation()) return

        queue.enqueue(item)
        processQueue()
    }

    fun onNotificationRemoved(key: String) {
        queue.remove(key)
        if (_currentItem.value?.key == key) {
            _currentItem.value = null
            pendingKey = null
            collapseTimerJob?.cancel()
            processQueue()
        }
    }

    private fun processQueue() {
        when (_state.value) {
            IslandState.Hidden -> {
                val topItem = queue.peek()
                if (topItem != null && _islandEnabled.value) {
                    showPill(topItem)
                }
            }
            IslandState.Pill -> {
                val topItem = queue.peek()
                val currentKey = _currentItem.value?.key
                if (topItem?.key != currentKey) {
                    if (topItem != null) {
                        showPill(topItem)
                    } else {
                        hideIsland()
                    }
                }
            }
            else -> { }
        }
    }

    private fun showPill(item: NotificationItem) {
        _currentItem.value = item
        pendingKey = item.key
        overlayWindowManager.showPill(item)

        if (_state.value != IslandState.Pill) {
            _state.value = IslandState.Pill
        }

        startCollapseTimer(item.key)
    }

    private fun startCollapseTimer(key: String) {
        collapseTimerJob?.cancel()

        if (key == pendingKey && _state.value == IslandState.Pill) {
            val item = queue.getItem(key)
            if (item?.priorityLevel == com.suprasidh.dynotifs.domain.model.PriorityLevel.TIER_2_MESSAGE) {
                collapseTimerJob = scope.launch {
                    delay(COLLAPSE_DELAY_MS)
                    if (key == pendingKey && _state.value == IslandState.Pill) {
                        queue.remove(key)
                        _currentItem.value = null
                        pendingKey = null
                        processQueue()
                    }
                }
            }
        }
    }

    fun onTouchReceived() {
        collapseTimerJob?.cancel()
    }

    fun expandIsland() {
        collapseTimerJob?.cancel()
        val item = _currentItem.value ?: return
        _state.value = IslandState.Expanded
        overlayWindowManager.showExpanded(item)
    }

    fun collapseIsland() {
        val item = _currentItem.value ?: return
        _state.value = IslandState.Pill
        overlayWindowManager.showPill(item)

        if (item.priorityLevel == com.suprasidh.dynotifs.domain.model.PriorityLevel.TIER_2_MESSAGE) {
            startCollapseTimer(item.key)
        }
    }

    fun hideIsland() {
        collapseTimerJob?.cancel()
        _state.value = IslandState.Hidden
        _currentItem.value = null
        pendingKey = null
        overlayWindowManager.hide()
    }

    fun setIslandEnabled(enabled: Boolean) {
        scope.launch {
            dataStore.setIslandEnabled(enabled)
        }
    }

    private fun checkOrientation(): Boolean {
        val config = overlayWindowManager.getContext().resources.configuration
        return config.orientation != Configuration.ORIENTATION_LANDSCAPE
    }
}