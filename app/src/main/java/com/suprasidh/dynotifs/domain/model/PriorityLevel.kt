package com.suprasidh.dynotifs.domain.model

import android.app.Notification

enum class PriorityLevel {
    TIER_1_CALL,
    TIER_2_MESSAGE,
    TIER_3_TIMER,
    TIER_4_MEDIA,
    TIER_5_OTHER
}

fun Notification.category?.toPriorityLevel(): PriorityLevel {
    return when (this) {
        Notification.CATEGORY_CALL -> PriorityLevel.TIER_1_CALL
        Notification.CATEGORY_MESSAGE -> PriorityLevel.TIER_2_MESSAGE
        Notification.CATEGORY_ALARM,
        Notification.CATEGORY_TIMER -> PriorityLevel.TIER_3_TIMER
        Notification.CATEGORY_TRANSPORT,
        Notification.CATEGORY_SERVICE -> PriorityLevel.TIER_4_MEDIA
        else -> PriorityLevel.TIER_5_OTHER
    }
}