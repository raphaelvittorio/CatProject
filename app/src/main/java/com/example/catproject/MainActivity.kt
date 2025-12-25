package com.example.catproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.catproject.ui.theme.CatProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatProjectTheme {
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = "login") {

                    // --- SCREEN UTAMA ---
                    composable("login") { LoginScreen(nav) }
                    composable("signup") { SignUpScreen(nav) }
                    composable("main_app") { MainAppScreen(nav) }
                }
            }
        }
    }
}