package com.suprasidh.dynotifs.data.model

data class CalibrationData(
    val offsetXPercent: Float = 0.5f,
    val offsetYPercent: Float = 0.02f,
    val widthPercent: Float = 0.30f,
    val heightPercent: Float = 0.05f
) {
    companion object {
        val DEFAULT = CalibrationData()
    }
}