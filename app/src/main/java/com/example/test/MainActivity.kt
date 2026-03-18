package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.navigation.NavHost
import com.example.test.ui.screens.DrawerMenu
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val navController = rememberNavController()

            val scope = rememberCoroutineScope()

            var showDetection by remember { mutableStateOf(false) }
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerMenu(
                        navController    = navController,
                        onClose          = { scope.launch { drawerState.close() } },
                        onDetectionClick = { showDetection = true }
                    )
                }
            ) {
            NavHost()
            }
        }
    }
}