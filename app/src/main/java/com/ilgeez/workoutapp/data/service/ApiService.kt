package com.ilgeez.workoutapp.data.service

import com.ilgeez.workoutapp.data.model.WorkoutResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/v2/interval-timers/{id}")
    suspend fun getWorkout(@Path("id") id: String): WorkoutResponse
}
