package com.ilgeez.workoutapp.ui.navigation

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ilgeez.workoutapp.ui.screens.login.LoginScreen
import com.ilgeez.workoutapp.ui.screens.workout.WorkoutScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Workout : Screen("workout/{timerId}") {
        fun createRoute(id: Int) = "workout/${id}"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { timer ->
                    navController.navigate(Screen.Workout.createRoute(timer.timerId))
                }
            )
        }

        composable(
            route = Screen.Workout.route,
            arguments = listOf(navArgument("timerId") { type = NavType.IntType })
        ) {
            WorkoutScreen()
        }
    }
}
