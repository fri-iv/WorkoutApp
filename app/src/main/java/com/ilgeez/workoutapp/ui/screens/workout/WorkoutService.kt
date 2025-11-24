package com.ilgeez.workoutapp.ui.screens.workout

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ilgeez.workoutapp.R
import com.ilgeez.workoutapp.data.model.TimerModel
import com.ilgeez.workoutapp.data.repository.WorkoutRepository
import com.ilgeez.workoutapp.utils.formatTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    @Inject
    lateinit var repository: WorkoutRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "START" -> {
                val timerId = intent.getIntExtra("TIMER_ID", -1)
                if (timerId != -1) {
                    val model = repository.getTimerModel(timerId.toString())
                    if (model != null) {
                        startWorkout(model)
                    } else {
                        stopSelf()
                    }
                }
            }

            "STOP" -> {
                stopWorkout()
            }
        }
        return START_STICKY
    }

    private fun startWorkout(model: TimerModel) {
        WorkoutStateManager.update {
            WorkoutState(
                isRunning = true,
                isFinished = false,
                timer = model,
                currentInterval = 0,
                currentIntervalTime = 0,
                totalTime = 0
            )
        }

        val notification = createNotification("Тренировка: ${model.title}")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, notification)
        }

        startTimerLoop(model)
    }

    private fun startTimerLoop(model: TimerModel) {
        timerJob?.cancel()
        timerJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    delay(1000L)

                    val currentState = WorkoutStateManager.getState().value
                    if (!currentState.isRunning || currentState.isFinished) {
                        break
                    }

                    val currentInterval = model.intervals[currentState.currentInterval]
                    if (currentState.currentIntervalTime >= currentInterval.time) {
                        if (currentState.currentInterval < model.intervals.lastIndex) {
                            WorkoutStateManager.update {
                                it.copy(
                                    currentInterval = it.currentInterval + 1,
                                    currentIntervalTime = 0
                                )
                            }
                        } else {
                            finishWorkout()
                        }
                    } else {
                        WorkoutStateManager.update {
                            it.copy(
                                currentIntervalTime = it.currentIntervalTime + 1,
                                totalTime = it.totalTime + 1
                            )
                        }

                        updateNotification("Время: ${formatTime(currentState.totalTime + 1)}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private fun finishWorkout() {
        WorkoutStateManager.update { it.copy(isRunning = false, isFinished = true) }
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun stopWorkout() {
        timerJob?.cancel()
        WorkoutStateManager.update { it.copy(isRunning = false) }
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, "workout_channel")
            .setContentTitle("Тренировка активна")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }


    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("workout_channel", "Workout", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
