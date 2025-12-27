package com.example.catproject

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.EventAvailable // atau ikon lain yg cocok
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    rootNavController: NavController // Parameter ini tidak wajib jika pakai Intent, tapi biarkan saja
) {
    val user = UserSession.currentUser
    val isAdmin = user?.role == "admin"

    // 1. Ambil Context untuk melakukan Restart
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // --- GENERAL SETTINGS ---
            // Item Account & Privacy dihapus sesuai permintaan

            SettingsItem(Icons.Outlined.BookmarkBorder, "Saved Posts") {
                navController.navigate("saved_posts")
            }
            SettingsItem(Icons.Default.EventAvailable, "Joined Events") {
                navController.navigate("joined_events")
            }

            // --- ADMIN SECTION ---
            if (isAdmin) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Admin Control",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                SettingsItem(
                    icon = Icons.Rounded.AdminPanelSettings,
                    title = "Admin Dashboard",
                    textColor = Color(0xFFFF9800)
                ) {
                    navController.navigate("admin_dashboard")
                }
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(color = Color(0xFFF5F5F5))

            // --- LOGOUT BUTTON (ANTI CRASH VERSION) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 1. Hapus Sesi
                        UserSession.currentUser = null

                        // 2. RESTART APLIKASI (Cara paling aman agar tidak crash)
                        val intent = Intent(context, MainActivity::class.java)
                        // Flag ini menghapus semua history layar sebelumnya
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)

                        // Menutup activity saat ini agar tidak bisa di-back
                        (context as? Activity)?.finish()
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "Log Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.ChevronRight, null, tint = Color.LightGray)
    }
}