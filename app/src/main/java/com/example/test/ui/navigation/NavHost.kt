package com.example.test.ui.navigation

import com.example.test.ui.screens.LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.test.ui.screens.*
import com.example.test.ui.viewmodel.UserViewModel

@Composable
fun NavHost(modifier: Modifier = Modifier) {

    val navControlleur = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navControlleur,
        startDestination = Routers.LOGIN,
        modifier = modifier
    ) {

        composable(Routers.LOGIN) {
            LoginScreen(
                navController = navControlleur,
                userViewModel = userViewModel
            )
        }

        composable(Routers.HOME) {
            HomeScreen(
                navController = navControlleur,
            )
        }

        composable(Routers.REGISTER) {
            RegisterScreen(
                navController = navControlleur

            )
        }

        composable(Routers.PROFILE) {
            ProfileScreen(
                navController = navControlleur,
                userViewModel = userViewModel                 // besoin de données utilisateur
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
                navController = navControlleur,
                userViewModel = userViewModel   // besoin de données utilisateur
            )
        }

        composable(Routers.APROPOS) {
            AProposScreen(
                navController = navControlleur
            )
        }

        composable(Routers.USER) {
            UserScreen(
                navController = navControlleur,
                userViewModel = userViewModel   // besoin de données utilisateur
            )
        }

        composable(Routers.FINGER_DRAW) {
            FingerDrawScreen(
                navController = navControlleur
            )
        }

        composable(Routers.PRET) {
            ContracterPret(
                navController = navControlleur
            )
        }

    }
}