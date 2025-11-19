package com.ilgeez.workoutapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilgeez.workoutapp.data.model.TimerModel
import com.ilgeez.workoutapp.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginState {
    object Idle : LoginState // default
    object OnProgress : LoginState
    data class OnSuccess(val response: TimerModel) : LoginState
    data class OnError(val message: String?) : LoginState
}

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: WorkoutRepository) : ViewModel() {
    private val workoutId = MutableStateFlow("68")
    private val state = MutableStateFlow<LoginState>(LoginState.Idle)

    fun getWorkoutId() = workoutId
    fun getState() = state

    fun onIdChange(id: String) {
        this.workoutId.value = id

        if (state.value is LoginState.OnError) {
            resetState()
        }
    }

    fun loadWorkout() {
        if (workoutId.value.isEmpty()) {
            return
        }

        viewModelScope.launch {
            state.value = LoginState.OnProgress
            try {
                val response = repository.loadTimer(workoutId.value)
                state.value = LoginState.OnSuccess(response)
            } catch (ex: Exception) {
                state.value = LoginState.OnError(ex.message)
            }
        }
    }

    fun resetState() {
        state.value = LoginState.Idle
    }
}