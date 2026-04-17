package com.suprasidh.dynotifs.domain.queue

import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.PriorityLevel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PriorityQueueTest {
    private lateinit var queue: PriorityNotificationQueue

    @Before
    fun setup() { queue = PriorityNotificationQueue() }

    private fun createItem(key: String, priority: PriorityLevel) = NotificationItem(
        key = key, packageName = "test", title = "Test", text = "Text",
        icon = null, category = null, priorityLevel = priority, contentIntent = null
    )

    @Test fun testCallOverridesMessage() {
        queue.enqueue(createItem("msg", PriorityLevel.TIER_2_MESSAGE))
        queue.enqueue(createItem("call", PriorityLevel.TIER_1_CALL))
        assertEquals(PriorityLevel.TIER_1_CALL, queue.peek()?.priorityLevel)
    }

    @Test fun testDequeueRemovesTop() {
        queue.enqueue(createItem("item1", PriorityLevel.TIER_2_MESSAGE))
        queue.enqueue(createItem("item2", PriorityLevel.TIER_2_MESSAGE))
        queue.dequeue()
        assertEquals(1, queue.queue.value.size)
    }

    @Test fun testRemoveSpecificKey() {
        queue.enqueue(createItem("remove", PriorityLevel.TIER_2_MESSAGE))
        queue.remove("remove")
        assertTrue(queue.isEmpty())
    }

    @Test fun testPeekEmpty() { assertNull(queue.peek()) }

    @Test fun testContains() {
        queue.enqueue(createItem("exists", PriorityLevel.TIER_1_CALL))
        assertTrue(queue.contains("exists"))
        assertFalse(queue.contains("notexists"))
    }

    @Test fun testClear() {
        queue.enqueue(createItem("a", PriorityLevel.TIER_1_CALL))
        queue.enqueue(createItem("b", PriorityLevel.TIER_2_MESSAGE))
        queue.clear()
        assertTrue(queue.isEmpty())
    }
}