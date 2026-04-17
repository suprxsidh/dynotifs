package com.suprasidh.dynotifs.domain.model

enum class PriorityLevel {
    TIER_1_CALL,
    TIER_2_MESSAGE,
    TIER_3_TIMER,
    TIER_4_MEDIA,
    TIER_5_OTHER
}

enum class IslandState {
    HIDDEN,
    PILL,
    EXPANDED
}

fun getPriorityLevel(category: String?): PriorityLevel {
    return when (category) {
        "call" -> PriorityLevel.TIER_1_CALL
        "message" -> PriorityLevel.TIER_2_MESSAGE
        "alarm", "timer" -> PriorityLevel.TIER_3_TIMER
        "transport", "service" -> PriorityLevel.TIER_4_MEDIA
        else -> PriorityLevel.TIER_5_OTHER
    }
}