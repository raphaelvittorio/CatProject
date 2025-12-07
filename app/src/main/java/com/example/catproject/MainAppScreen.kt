package com.example.catproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Item: Home, Search, Add, Reels, Profile
                NavigationBarItem(
                    selected = currentRoute == "home", onClick = { navController.navigate("home") },
                    icon = { Icon(if(currentRoute=="home") Icons.Filled.Home else Icons.Outlined.Home, null) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black)
                )
                NavigationBarItem(
                    selected = currentRoute == "search", onClick = { navController.navigate("search") },
                    icon = { Icon(Icons.Default.Search, null) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black)
                )
                NavigationBarItem(
                    selected = currentRoute == "add", onClick = { navController.navigate("add") },
                    icon = { Icon(Icons.Outlined.AddBox, null) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black)
                )
                NavigationBarItem(
                    selected = currentRoute == "reels", onClick = { navController.navigate("reels") },
                    icon = { Icon(Icons.Outlined.Movie, null) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black)
                )
                NavigationBarItem(
                    selected = currentRoute == "profile", onClick = { navController.navigate("profile") },
                    icon = { Icon(if(currentRoute=="profile") Icons.Filled.Person else Icons.Outlined.Person, null) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black)
                )
            }
        }
    ) { p ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(p)) {
            composable("home") { HomeScreen() }
            composable("search") { ExploreScreen() }
            composable("add") { AddPostScreen(navController) } // Gunakan file AddPostScreen sebelumnya
            composable("reels") { ReelsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
fun ReelsScreen() {
    // Placeholder layar hitam sederhana untuk Reels
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Reels Video Placeholder", color = Color.White)
    }
}