package com.suprasidh.dynotifs.domain.queue

import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.PriorityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriorityNotificationQueue @Inject constructor() {

    private val _queue = MutableStateFlow<List<NotificationItem>>(emptyList())
    val queue: StateFlow<List<NotificationItem>> = _queue.asStateFlow()

    private val priorityOrder = listOf(
        PriorityLevel.TIER_1_CALL,
        PriorityLevel.TIER_2_MESSAGE,
        PriorityLevel.TIER_3_TIMER,
        PriorityLevel.TIER_4_MEDIA,
        PriorityLevel.TIER_5_OTHER
    )

    fun enqueue(item: NotificationItem) {
        val currentList = _queue.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.key == item.key }
        if (existingIndex >= 0) {
            currentList[existingIndex] = item
        } else {
            val insertIndex = currentList.indexOfFirst { priorityOrder.indexOf(it.priorityLevel) > priorityOrder.indexOf(item.priorityLevel) }
            if (insertIndex >= 0) {
                currentList.add(insertIndex, item)
            } else {
                currentList.add(item)
            }
        }
        _queue.value = currentList
    }

    fun peek(): NotificationItem? = _queue.value.firstOrNull()

    fun dequeue(): NotificationItem? {
        val item = _queue.value.firstOrNull() ?: return null
        _queue.value = _queue.value.drop(1)
        return item
    }

    fun remove(key: String) {
        _queue.value = _queue.value.filter { it.key != key }
    }

    fun clear() {
        _queue.value = emptyList()
    }

    fun contains(key: String): Boolean = _queue.value.any { it.key == key }

    fun getTopItem(): NotificationItem? = _queue.value.firstOrNull()

    fun isEmpty(): Boolean = _queue.value.isEmpty()

    fun getItem(key: String): NotificationItem? = _queue.value.find { it.key == key }
}