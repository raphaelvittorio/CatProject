package com.example.catproject

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home_feed", Icons.Default.Home, "Home")
    object Search : BottomNavItem("search", Icons.Default.Search, "Search")
    object Add : BottomNavItem("add_post", Icons.Default.AddBox, "Add")
    object Activity : BottomNavItem("activity", Icons.Default.Favorite, "Activity")
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profile")
}