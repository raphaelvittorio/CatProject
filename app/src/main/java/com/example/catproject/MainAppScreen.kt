package com.example.catproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
// Import ikon versi Rounded dan Outlined yang modern
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainAppScreen(rootNavController: NavController) {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp // Menghilangkan bayangan agar flat modern
            ) {
                val navBackStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // DATA NAVIGASI MODERN
                // Format: Route, Icon Aktif (Filled), Icon Tidak Aktif (Outlined), Label
                val items = listOf(
                    Quadruple("home", Icons.Rounded.Home, Icons.Outlined.Home, "Home"),
                    Quadruple("search", Icons.Rounded.Search, Icons.Outlined.Search, "Search"),
                    Quadruple("add", Icons.Rounded.AddCircle, Icons.Outlined.AddCircleOutline, "Add"),
                    // Menggunakan Pets untuk Adopt (bisa diganti jika ada icon lain yang lebih cocok)
                    Quadruple("adopt", Icons.Rounded.Pets, Icons.Outlined.Pets, "Adopt"),
                    Quadruple("profile", Icons.Rounded.Person, Icons.Outlined.PersonOutline, "Profile")
                )

                items.forEach { (route, selectedIcon, unselectedIcon, label) ->
                    val isSelected = currentRoute == route
                    NavigationBarItem(
                        // Logika Icon: Jika dipilih pakai yang Filled, jika tidak pakai Outlined
                        icon = {
                            Icon(
                                if (isSelected) selectedIcon else unselectedIcon,
                                contentDescription = label
                            )
                        },
                        // Menghilangkan label teks agar lebih minimalis (opsional, hapus baris ini jika ingin tetap ada label)
                        label = { Text(label) },
                        selected = isSelected,
                        onClick = {
                            nav.navigate(route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Hilangkan lingkaran highlight
                            selectedIconColor = Color(0xFFFF9800), // Warna Orange saat aktif
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFFFF9800),
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ... (SEMUA RUTE DI BAWAH INI SAMA PERSIS DENGAN SEBELUMNYA) ...
            composable("home") { HomeScreen(nav) }
            composable("search") { ExploreScreen(nav) }
            composable("add") { AddPostScreen(nav) }
            composable("adopt") { AdoptScreen(nav) }
            composable("add_adopt") { AddAdoptScreen(nav) }
            composable(route = "apply_adopt/{adoptId}/{catName}", arguments = listOf(navArgument("adoptId") { type = NavType.IntType }, navArgument("catName") { type = NavType.StringType })) { backStackEntry -> val id = backStackEntry.arguments?.getInt("adoptId") ?: 0; val name = backStackEntry.arguments?.getString("catName") ?: "Cat"; ApplyAdoptionScreen(nav, id, name) }
            composable("profile") { ProfileScreen(nav, rootNavController, null) }
            composable(route = "visit_profile/{userId}", arguments = listOf(navArgument("userId") { type = NavType.IntType })) { backStackEntry -> val uid = backStackEntry.arguments?.getInt("userId") ?: 0; ProfileScreen(nav, rootNavController, uid) }
            composable("edit_profile") { EditProfileScreen(nav) }
            composable(route = "post_detail/{postId}", arguments = listOf(navArgument("postId") { type = NavType.IntType })) { backStackEntry -> val pid = backStackEntry.arguments?.getInt("postId") ?: 0; PostDetailScreen(nav, pid) }
            composable(route = "comments/{postId}", arguments = listOf(navArgument("postId") { type = NavType.IntType })) { backStackEntry -> val pid = backStackEntry.arguments?.getInt("postId") ?: 0; CommentsScreen(nav, pid) }
            composable("chat_list") { ChatListScreen(nav) }
            composable(route = "chat_detail/{userId}/{userName}", arguments = listOf(navArgument("userId") { type = NavType.IntType }, navArgument("userName") { type = NavType.StringType })) { backStackEntry -> val uid = backStackEntry.arguments?.getInt("userId") ?: 0; val uname = backStackEntry.arguments?.getString("userName") ?: "Chat"; ChatDetailScreen(nav, uid, uname) }
        }
    }
}

// Helper class kecil untuk menyimpan 4 data navbar
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)