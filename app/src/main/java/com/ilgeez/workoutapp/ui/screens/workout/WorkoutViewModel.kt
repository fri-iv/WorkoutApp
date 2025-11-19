package com.ilgeez.workoutapp.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ilgeez.workoutapp.data.model.TimerModel
import com.ilgeez.workoutapp.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

object WorkoutStateManager {
    private val state = MutableStateFlow(WorkoutState())

    fun update(callback: (WorkoutState) -> WorkoutState) {
        state.value = callback(state.value)
    }

    fun getState() = state.asStateFlow()
}

data class WorkoutState(
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val timer: TimerModel? = null,
    val currentInterval: Int = 0,
    val currentIntervalTime: Int = 0,
    val totalTime: Int = 0
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val timerId = checkNotNull(savedStateHandle["timerId"])

    init {
        initializeWorkout()
    }

    private fun initializeWorkout() {
        val timer = repository.getTimerModel(timerId.toString())
        val state = WorkoutStateManager.getState().value
        if (!state.isRunning && !state.isFinished) {
            WorkoutStateManager.update {
                it.copy(timer = timer)
            }
        }
    }
}
