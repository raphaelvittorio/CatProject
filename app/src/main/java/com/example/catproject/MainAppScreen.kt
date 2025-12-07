package com.example.catproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainAppScreen(rootNavController: NavController) {
    // NavController ini khusus untuk navigasi di dalam Tab Bar (Home, Search, Profile, dll)
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Daftar Item Tab Bawah
                val items = listOf(
                    Triple("home", Icons.Default.Home, "Home"),
                    Triple("search", Icons.Default.Search, "Search"),
                    Triple("add", Icons.Outlined.AddBox, "Add"),
                    Triple("adopt", Icons.Default.Pets, "Adopt"), // Ikon Paw untuk Adopsi
                    Triple("profile", Icons.Default.Person, "Profile")
                )

                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        selected = currentRoute == route,
                        onClick = {
                            bottomNavController.navigate(route) {
                                // Agar tidak menumpuk halaman saat diklik berkali-kali
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent, // Hilangkan highlight oval
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Container Utama yang berganti-ganti isinya
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. HOME FEED
            composable("home") {
                HomeScreen(bottomNavController)
            }

            // 2. SEARCH & EXPLORE USER
            composable("search") {
                ExploreScreen(bottomNavController)
            }

            // 3. UPLOAD POSTINGAN BARU
            composable("add") {
                AddPostScreen(bottomNavController)
            }

            // 4. ADOPSI KUCING
            composable("adopt") {
                AdoptScreen(bottomNavController)
            }

            // 4b. TAMBAH LIST ADOPSI
            composable("add_adopt") {
                AddAdoptScreen(bottomNavController)
            }

            // 5. PROFILE SAYA (Tanpa parameter UID)
            composable("profile") {
                ProfileScreen(
                    navController = bottomNavController,
                    rootNavController = rootNavController, // Untuk Logout
                    targetUserId = null // Null = Saya sendiri
                )
            }

            // 5b. VISIT PROFILE ORANG LAIN
            composable(
                route = "visit_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt("userId") ?: 0
                ProfileScreen(
                    navController = bottomNavController,
                    rootNavController = rootNavController,
                    targetUserId = uid // ID orang yang diklik
                )
            }

            // 5c. EDIT PROFILE
            composable("edit_profile") {
                EditProfileScreen(bottomNavController)
            }

            // 6. DETAIL POSTINGAN
            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getInt("postId") ?: 0
                PostDetailScreen(bottomNavController, pid)
            }

            // 7. KOMENTAR
            composable(
                route = "comments/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getInt("postId") ?: 0
                CommentsScreen(navController = bottomNavController, postId = pid)
            }

            // 8. INBOX LIST (Daftar Chat)
            composable("chat_list") {
                // PERBAIKAN: Gunakan bottomNavController, bukan nav
                ChatListScreen(bottomNavController)
            }

            // 9. CHAT DETAIL (Room Chat)
            composable(
                route = "chat_detail/{userId}/{userName}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt("userId") ?: 0
                val uname = backStackEntry.arguments?.getString("userName") ?: "Chat"
                // PERBAIKAN: Gunakan bottomNavController
                ChatDetailScreen(bottomNavController, uid, uname)
            }
        }
    }
}