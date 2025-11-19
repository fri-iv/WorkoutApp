package com.ilgeez.workoutapp.data.repository

import com.ilgeez.workoutapp.data.model.TimerModel
import com.ilgeez.workoutapp.data.service.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(private val api: ApiService) {
    private var timer: TimerModel? = null

    suspend fun loadTimer(id: String): TimerModel {
        val response = api.getWorkout(id)
        this.timer = response.timer
        return response.timer
    }

    fun getTimerModel(id: String): TimerModel? {
        return if (timer?.timerId.toString() == id) timer else null
    }
}
