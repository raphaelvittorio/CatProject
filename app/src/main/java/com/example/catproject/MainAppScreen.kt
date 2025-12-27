package com.example.catproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Event // Ikon Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Event // Ikon Event Outlined
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
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // LOGIKA MENYEMBUNYIKAN BOTTOM BAR
    // Bottom bar disembunyikan saat membuka Story, Chat Detail, Camera, dll.
    val hideBottomBarRoutes = listOf(
        "story_view/{userId}",
        "add",
        "chat_detail/{userId}/{userName}",
        "comments/{postId}"
    )

    // Cek apakah rute saat ini ada di daftar yang harus disembunyikan
    // Kita pakai statrtWith atau pencocokan pola sederhana agar parameter {userId} tetap terdeteksi
    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes &&
            currentRoute?.startsWith("story_view/") == false

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        Quadruple("home", Icons.Rounded.Home, Icons.Outlined.Home, "Home"),
                        Quadruple("search", Icons.Rounded.Search, Icons.Outlined.Search, "Search"),
                        Quadruple("adopt", Icons.Rounded.Pets, Icons.Outlined.Pets, "Adopt"),
                        Quadruple("event", Icons.Rounded.Event, Icons.Outlined.Event, "Events"),
                        Quadruple("profile", Icons.Rounded.Person, Icons.Outlined.PersonOutline, "Profile")
                    )

                    items.forEach { (route, selectedIcon, unselectedIcon, label) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            icon = { Icon(if (isSelected) selectedIcon else unselectedIcon, contentDescription = label) },
                            selected = isSelected,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = false }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = Color(0xFFFF9800),
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- ROUTES ---

            // 1. HOME (Termasuk Feed & List Story)
            composable("home") { HomeScreen(nav) }

            // 2. SEARCH
            composable("search") { ExploreScreen(nav) }

            // 3. EVENT
            composable("event") { EventScreen(nav) }
            composable("create_event") { CreateEventScreen(nav) }

            // 4. ADD POST
            composable("add") { AddPostScreen(nav) }

            // 5. ADOPT
            composable("adopt") { AdoptScreen(nav) }
            composable("add_adopt") { AddAdoptScreen(nav) }
            composable(
                route = "apply_adopt/{adoptId}/{catName}",
                arguments = listOf(navArgument("adoptId") { type = NavType.IntType }, navArgument("catName") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("adoptId") ?: 0
                val name = backStackEntry.arguments?.getString("catName") ?: "Cat"
                ApplyAdoptionScreen(nav, id, name)
            }

            // 6. PROFILE
            composable("profile") { ProfileScreen(nav, rootNavController, null) }
            composable(
                route = "visit_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt("userId") ?: 0
                ProfileScreen(nav, rootNavController, uid)
            }
            composable("edit_profile") { EditProfileScreen(nav) }

            // 7. POST DETAIL & COMMENT
            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getInt("postId") ?: 0
                PostDetailScreen(nav, pid)
            }
            composable(
                route = "comments/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getInt("postId") ?: 0
                CommentsScreen(nav, pid)
            }

            // 8. CHAT
            composable("chat_list") { ChatListScreen(nav) }
            composable(
                route = "chat_detail/{userId}/{userName}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType }, navArgument("userName") { type = NavType.StringType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt("userId") ?: 0
                val uname = backStackEntry.arguments?.getString("userName") ?: "Chat"
                ChatDetailScreen(nav, uid, uname)
            }

            // 9. NOTIFICATION
            composable("notifications") { NotificationScreen(nav) }

            // --- 10. STORY VIEW (ROUTE BARU DITAMBAHKAN DI SINI) ---
            composable(
                route = "story_view/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt("userId") ?: 0
                // Memanggil Screen Story yang sudah dibuat terpisah
                StoryViewScreen(nav, uid)
            }

            // Di dalam MainAppScreen -> NavHost
            composable("admin_dashboard") { AdminDashboardScreen(nav) }
            composable("admin_users") { AdminUserListScreen(nav) }
            composable("admin_posts") { AdminPostListScreen(nav) }
        }
    }
}

// Helper Class
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)