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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.catproject.network.DashboardStats
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }

    // Load Real Analytics
    LaunchedEffect(Unit) {
        try { stats = RetrofitClient.instance.getAdminDashboardStats() } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { p ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(p)
        ) {
            // --- SECTION 1: ANALYTICS CARDS (REAL DATA) ---
            item(span = { GridItemSpan(2) }) {
                Text("Analytics Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            item {
                StatCardModern(
                    title = "Total Users",
                    value = stats?.total_users ?: "-",
                    icon = Icons.Rounded.Group,
                    color = Color(0xFF2196F3)
                )
            }
            item {
                StatCardModern(
                    title = "Posts Created",
                    value = stats?.total_posts ?: "-",
                    icon = Icons.Rounded.Image,
                    color = Color(0xFF9C27B0)
                )
            }
            item {
                StatCardModern(
                    title = "Successful Adoptions",
                    value = stats?.successful_adoptions ?: "-",
                    icon = Icons.Rounded.Pets,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                // Card Reports dengan indikator merah jika ada pending
                StatCardModern(
                    title = "Pending Reports",
                    value = stats?.pending_reports ?: "-",
                    icon = Icons.Rounded.ReportProblem,
                    color = Color(0xFFF44336),
                    isAlert = (stats?.pending_reports?.toIntOrNull() ?: 0) > 0
                )
            }

            // --- SECTION 2: MANAGEMENT MENU ---
            item(span = { GridItemSpan(2) }) {
                Spacer(Modifier.height(16.dp))
                Text("Management Tools", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            // MENU: REPORTS (NEW)
            item(span = { GridItemSpan(2) }) {
                AdminActionCard(
                    title = "User Reports",
                    subtitle = "Review and moderate reported content",
                    icon = Icons.Rounded.Report,
                    gradientColors = listOf(Color(0xFFD32F2F), Color(0xFFFF5252)),
                    onClick = { navController.navigate("admin_reports") },
                    isWide = true
                )
            }

            // MENU: USERS
            item {
                AdminActionCard(
                    title = "Users",
                    subtitle = "Manage accounts",
                    icon = Icons.Rounded.PersonSearch,
                    gradientColors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
                    onClick = { navController.navigate("admin_users") }
                )
            }

            // MENU: POSTS
            item {
                AdminActionCard(
                    title = "Posts",
                    subtitle = "Content feed",
                    icon = Icons.Rounded.DynamicFeed,
                    gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF512F)),
                    onClick = { navController.navigate("admin_posts") }
                )
            }
        }
    }
}

@Composable
fun StatCardModern(title: String, value: String, icon: ImageVector, color: Color, isAlert: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Icon(icon, null, tint = color)
                if (isAlert) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                }
            }
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.Black)
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// Letakkan di paling bawah file AdminDashboardScreen.kt

@Composable
fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            // Background Gradient Bubble
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = if (isWide) 250.dp else 80.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.15f) }))
            )

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = if (isWide) Arrangement.Center else Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // ICON CONTAINER
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)),
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