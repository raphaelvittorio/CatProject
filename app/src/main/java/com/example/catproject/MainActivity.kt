package com.example.catproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catproject.ui.theme.CatProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatProjectTheme {
                // KITA DEFINISIKAN NAMANYA 'nav' DI SINI
                val nav = rememberNavController()

                // Navigasi Utama (Login <-> MainApp)
                NavHost(navController = nav, startDestination = "login") {

                    composable("login") {
                        LoginScreen(nav)
                    }

                    composable("signup") {
                        SignUpScreen(nav)
                    }

                    composable("main_app") {
                        MainAppScreen(nav)
                    }
                }
            }
        }
    }
}