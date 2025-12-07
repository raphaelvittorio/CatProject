package com.example.catproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(selected = currentRoute == "home", onClick = { navController.navigate("home") }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = currentRoute == "add", onClick = { navController.navigate("add") }, icon = { Icon(Icons.Default.AddBox, null) }, label = { Text("Add") })
                NavigationBarItem(selected = currentRoute == "profile", onClick = { navController.navigate("profile") }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { p ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(p)) {
            composable("home") { HomeScreen() }
            composable("add") { AddPostScreen(navController) }
            composable("profile") { ProfileScreen() }
        }
    }
}