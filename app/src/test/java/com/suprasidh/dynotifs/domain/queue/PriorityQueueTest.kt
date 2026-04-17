package com.suprasidh.dynotifs.domain.queue

import android.app.Notification
import android.app.PendingIntent
import com.suprasidh.dynotifs.domain.model.NotificationAction
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.PriorityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

class PriorityQueueTest {

    private lateinit var queue: PriorityNotificationQueue

    @Before
    fun setup() {
        queue = PriorityNotificationQueue()
    }

    private fun createNotificationItem(
        key: String,
        category: String?,
        priorityLevel: PriorityLevel
    ): NotificationItem {
        return NotificationItem(
            key = key,
            packageName = "test.package",
            title = "Test Title",
            text = "Test Text",
            icon = null,
            category = category,
            priorityLevel = priorityLevel,
            contentIntent = null
        )
    }

    @Test
    fun testCallOverridesMessage() {
        val call = createNotificationItem(
            key = "call_${ System.currentTimeMillis()}",
            category = Notification.CATEGORY_CALL,
            priorityLevel = PriorityLevel.TIER_1_CALL
        )
        val message = createNotificationItem(
            key = "msg_${ System.currentTimeMillis()}",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )

        queue.enqueue(message)
        queue.enqueue(call)

        assertEquals(PriorityLevel.TIER_1_CALL, queue.peek()?.priorityLevel)
    }

    @Test
    fun testMessageOverridesTimer() {
        val message = createNotificationItem(
            key = "msg_${ System.currentTimeMillis()}",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )
        val timer = createNotificationItem(
            key = "timer_${ System.currentTimeMillis()}",
            category = Notification.CATEGORY_TIMER,
            priorityLevel = PriorityLevel.TIER_3_TIMER
        )

        queue.enqueue(timer)
        queue.enqueue(message)

        assertEquals(PriorityLevel.TIER_2_MESSAGE, queue.peek()?.priorityLevel)
    }

    @Test
    fun testDequeueRemovesTopItem() {
        val item1 = createNotificationItem(
            key = "msg1",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )
        val item2 = createNotificationItem(
            key = "msg2",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )

        queue.enqueue(item1)
        queue.enqueue(item2)

        queue.dequeue()
        assertEquals(1, queue.queue.value.size)
    }

    @Test
    fun testRemoveSpecificKey() {
        val item = createNotificationItem(
            key = "removeme",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )

        queue.enqueue(item)
        queue.remove("removeme")

        assertTrue(queue.isEmpty())
    }

    @Test
    fun testPeekReturnsNullForEmptyQueue() {
        assertNull(queue.peek())
    }

    @Test
    fun testContains() {
        val item = createNotificationItem(
            key = "checkme",
            category = Notification.CATEGORY_CALL,
            priorityLevel = PriorityLevel.TIER_1_CALL
        )

        queue.enqueue(item)

        assertTrue(queue.contains("checkme"))
        assertFalse(queue.contains("notpresent"))
    }

    @Test
    fun testUpdateExistingItem() {
        val item1 = createNotificationItem(
            key = "samekey",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        )
        val item2 = createNotificationItem(
            key = "samekey",
            category = Notification.CATEGORY_CALL,
            priorityLevel = PriorityLevel.TIER_1_CALL
        )

        queue.enqueue(item1)
        queue.enqueue(item2)

        assertEquals(1, queue.queue.value.size)
        assertEquals(PriorityLevel.TIER_1_CALL, queue.peek()?.priorityLevel)
    }

    @Test
    fun testClearEmptiesQueue() {
        queue.enqueue(createNotificationItem(
            key = "item1",
            category = Notification.CATEGORY_MESSAGE,
            priorityLevel = PriorityLevel.TIER_2_MESSAGE
        ))
        queue.enqueue(createNotificationItem(
            key = "item2",
            category = Notification.CATEGORY_CALL,
            priorityLevel = PriorityLevel.TIER_1_CALL
        ))

        queue.clear()

        assertTrue(queue.isEmpty())
    }
}