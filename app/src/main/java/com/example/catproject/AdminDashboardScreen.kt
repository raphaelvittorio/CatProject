package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    val brandColor = Color(0xFFFF9800)
    val user = UserSession.currentUser

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF8F9FA) // Background Modern (Soft Gray)
    ) { p ->
        // Menggunakan LazyVerticalGrid untuk layout utama agar responsif
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
        ) {
            // --- HEADER SECTION (Full Width) ---
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Hello, ${user?.username ?: "Admin"}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Select a tool below to manage the platform contents and users.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )
                }
            }

            // --- MENU CARDS ---

            // 1. Manage Users
            item {
                AdminActionCard(
                    title = "Users",
                    subtitle = "Manage roles & accounts",
                    icon = Icons.Rounded.PersonSearch,
                    gradientColors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)), // Purple-Blue
                    onClick = { navController.navigate("admin_users") }
                )
            }

            // 2. Manage Posts
            item {
                AdminActionCard(
                    title = "Posts",
                    subtitle = "Moderate content feed",
                    icon = Icons.Rounded.DynamicFeed,
                    gradientColors = listOf(Color(0xFFFD1D1D), Color(0xFFFCB045)), // Red-Orange
                    onClick = { navController.navigate("admin_posts") }
                )
            }

            // 3. Create Event (Full Width)
            item(span = { GridItemSpan(2) }) {
                AdminActionCard(
                    title = "Create Community Event",
                    subtitle = "Host a new gathering or webinar",
                    icon = Icons.Rounded.EventAvailable,
                    gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF512F)), // Brand Orange
                    onClick = { navController.navigate("create_event") },
                    isWide = true
                )
            }
        }
    }
}

// --- MODERN CARD COMPONENT ---

@Composable
fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    isWide: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isWide) 120.dp else 180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Gradient Bubble (Decorative)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = if (isWide) 250.dp else 80.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.15f) }))
            )

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                // Vertikal sudah diatur di sini (Center jika isWide)
                verticalArrangement = if (isWide) Arrangement.Center else Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // ICON CONTAINER
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (!isWide) Spacer(Modifier.height(12.dp))

                // TEXT CONTENT
                Column(
                    // PERBAIKAN DI SINI: Gunakan Alignment.Start (Horizontal saja)
                    modifier = if (isWide) Modifier
                        .padding(start = 64.dp)
                        .align(Alignment.Start)
                    else Modifier
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}