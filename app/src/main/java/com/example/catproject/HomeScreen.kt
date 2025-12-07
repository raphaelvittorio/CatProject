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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.*

// IMPORT IKON MODERN
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send

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
                title = {
                    Text(
                        text = "CatPaw",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = (-1).sp,
                        color = Color(0xFFFF9800) // Orange Brand Color
                    )
                },
                actions = {
                    // UPDATE: Ikon Pesawat Kertas Modern untuk Inbox
                    IconButton(onClick = { navController.navigate("chat_list") }) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Messages", tint = Color.Black)
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