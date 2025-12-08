package com.example.catproject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.*

// --- IMPORT IKON MODERN ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val myId = UserSession.currentUser?.id ?: 0

    LaunchedEffect(Unit) {
        try { posts = RetrofitClient.instance.getPosts(myId) } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                // 1. LOGO TENGAH
                title = {
                    Text(
                        text = "CatPaw",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = (-1).sp,
                        color = Color(0xFFFF9800)
                    )
                },

                // 2. IKON KIRI: PLUS SAJA (Tanpa Border)
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("add") }) {
                        Icon(
                            imageVector = Icons.Rounded.Add, // Menggunakan Rounded.Add
                            contentDescription = "New Post",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp) // Ukuran diperbesar sedikit agar pas
                        )
                    }
                },

                // 3. IKON KANAN: NOTIFIKASI & CHAT
                actions = {
                    // IKON NOTIFIKASI (Hati)
                    IconButton(onClick = {
                        // UPDATE: Navigasi ke halaman notifikasi
                        navController.navigate("notifications")
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Notifications",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Ikon Chat
                    IconButton(onClick = { navController.navigate("chat_list") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Messages",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.fillMaxSize()) {
            items(posts) { post ->
                PostItem(post, navController, allowDelete = false)
            }
        }
    }
}