package com.example.catproject
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.*
import androidx.navigation.compose.*

@Composable
fun MainAppScreen(rootNav: NavController) {
    val nav = rememberNavController()
    Scaffold(bottomBar = {
        NavigationBar(containerColor = Color.White) {
            val route = nav.currentBackStackEntryAsState().value?.destination?.route
            listOf("home" to Icons.Default.Home, "search" to Icons.Default.Search, "add" to Icons.Outlined.AddBox, "reels" to Icons.Outlined.Movie, "profile" to Icons.Default.Person).forEach { pair ->
                NavigationBarItem(selected = route == pair.first, onClick = { nav.navigate(pair.first) }, icon = { Icon(pair.second, null) }, colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, selectedIconColor = Color.Black, unselectedIconColor = Color.Black))
            }
        }
    }) { p ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(p)) {
            composable("home") { HomeScreen(nav) }
            composable("search") { ExploreScreen() }
            composable("add") { AddPostScreen(nav) }
            composable("reels") { ReelsScreen() }
            composable("profile") { ProfileScreen() }
            composable("comments/{id}", arguments = listOf(navArgument("id"){type=NavType.IntType})) {
                CommentsScreen(it.arguments?.getInt("id")?:0)
            }
        }
    }
}