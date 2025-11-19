package com.ilgeez.workoutapp.data.model

import com.google.gson.annotations.SerializedName

data class WorkoutResponse(
    @SerializedName("timer") val timer: TimerModel
)

data class TimerModel(
    @SerializedName("timer_id") val timerId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("total_time") val totalTime: Int,
    @SerializedName("intervals") val intervals: List<IntervalModel>,
)

data class IntervalModel(
    @SerializedName("title") val title: String,
    @SerializedName("time") val time: Int
)
