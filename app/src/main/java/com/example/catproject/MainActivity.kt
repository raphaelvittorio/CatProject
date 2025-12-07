package com.example.catproject
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.catproject.ui.theme.CatProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatProjectTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "login") {
                    composable("login") { LoginScreen(nav) }
                    composable("signup") { SignUpScreen(nav) }
                    composable("main_app") { MainAppScreen(nav) }
                }
            }
        }
    }
}