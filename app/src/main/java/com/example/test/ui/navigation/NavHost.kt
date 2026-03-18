package com.example.test.ui.navigation

import com.example.test.ui.screens.LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.test.ui.screens.*

@Composable
fun NavHost(modifier: Modifier = Modifier) {

    val navControlleur = rememberNavController()

    NavHost(
        navController = navControlleur,
        startDestination = Routers.LOGIN,
        modifier = modifier
    ) {

        composable(Routers.LOGIN) {
            LoginScreen(
                navController = navControlleur
            )
        }

        composable(Routers.HOME) {
            HomeScreen(
                navController = navControlleur
            )
        }

        composable(Routers.REGISTER) {
            RegisterScreen(
                navController = navControlleur
            )
        }

        composable(Routers.PROFILE) {
            ProfileScreen(
                navController = navControlleur
            )
        }

        composable(Routers.DETECTION) {
            DetectionScreen(
                navController = navControlleur
            )
        }
        composable(Routers.DETECTIONS) {
            DetectionsScreen(
                navController = navControlleur
            )
        }

        composable(Routers.POSTS) {
            PostScreen(
                navController = navControlleur
            )
        }

    }
}