package com.suprasidh.dynotifs.domain.model

import android.app.Notification

enum class PriorityLevel {
    TIER_1_CALL,
    TIER_2_MESSAGE,
    TIER_3_TIMER,
    TIER_4_MEDIA,
    TIER_5_OTHER
}

fun getPriorityLevel(category: String?): PriorityLevel {
    return when (category) {
        Notification.CATEGORY_CALL -> PriorityLevel.TIER_1_CALL
        Notification.CATEGORY_MESSAGE -> PriorityLevel.TIER_2_MESSAGE
        Notification.CATEGORY_ALARM -> PriorityLevel.TIER_3_TIMER
        "timer" -> PriorityLevel.TIER_3_TIMER
        Notification.CATEGORY_TRANSPORT,
        Notification.CATEGORY_SERVICE -> PriorityLevel.TIER_4_MEDIA
        else -> PriorityLevel.TIER_5_OTHER
    }
}