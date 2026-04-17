package com.suprasidh.dynotifs.overlay

import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.domain.model.IslandState
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.PriorityLevel
import com.suprasidh.dynotifs.domain.queue.PriorityNotificationQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IslandStateMachine @Inject constructor(
    private val queue: PriorityNotificationQueue,
    private val dataStore: DynotifsDataStore,
    private val overlayManager: OverlayWindowManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _state = MutableStateFlow(IslandState.HIDDEN)
    val state: StateFlow<IslandState> = _state.asStateFlow()

    private val _currentItem = MutableStateFlow<NotificationItem?>(null)
    val currentItem: StateFlow<NotificationItem?> = _currentItem.asStateFlow()

    private var collapseTimer: Job? = null
    private var pendingKey: String? = null

    companion object {
        private const val COLLAPSE_DELAY = 5000L
    }

    fun onNotificationPosted(item: NotificationItem) {
        // Skip if island disabled - just don't process
        queue.enqueue(item)
        processQueue()
    }

    fun onNotificationRemoved(key: String) {
        queue.remove(key)
        if (_currentItem.value?.key == key) {
            _currentItem.value = null
            pendingKey = null
            collapseTimer?.cancel()
            processQueue()
        }
    }

    private fun processQueue() {
        when (_state.value) {
            IslandState.HIDDEN -> {
                queue.peek()?.let { showPill(it) }
            }
            IslandState.PILL -> {
                val top = queue.peek()
                val currentKey = _currentItem.value?.key
                if (top?.key != currentKey) {
                    if (top != null) showPill(top) else hide()
                }
            }
            IslandState.EXPANDED -> { /* Keep expanded */ }
        }
    }

    private fun showPill(item: NotificationItem) {
        _currentItem.value = item
        pendingKey = item.key
        overlayManager.showPill(item)
        _state.value = IslandState.PILL

        startCollapseTimer(item.key)
    }

    private fun startCollapseTimer(key: String) {
        collapseTimer?.cancel()
        if (key == pendingKey && _state.value == IslandState.PILL) {
            val item = queue.getItem(key)
            if (item?.priorityLevel == PriorityLevel.TIER_2_MESSAGE) {
                collapseTimer = scope.launch {
                    delay(COLLAPSE_DELAY)
                    if (key == pendingKey && _state.value == IslandState.PILL) {
                        queue.remove(key)
                        _currentItem.value = null
                        pendingKey = null
                        processQueue()
                    }
                }
            }
        }
    }

    fun onTouch() {
        collapseTimer?.cancel()
    }

    fun expand() {
        collapseTimer?.cancel()
        _currentItem.value?.let {
            _state.value = IslandState.EXPANDED
            overlayManager.showExpanded(it)
        }
    }

    fun collapse() {
        val item = _currentItem.value ?: return
        _state.value = IslandState.PILL
        overlayManager.showPill(item)
        if (item.priorityLevel == PriorityLevel.TIER_2_MESSAGE) {
            startCollapseTimer(item.key)
        }
    }

    fun hide() {
        collapseTimer?.cancel()
        _state.value = IslandState.HIDDEN
        _currentItem.value = null
        pendingKey = null
        overlayManager.hide()
    }

    fun setEnabled(enabled: Boolean) {
        scope.launch { dataStore.setIslandEnabled(enabled) }
        if (!enabled) hide()
    }
}