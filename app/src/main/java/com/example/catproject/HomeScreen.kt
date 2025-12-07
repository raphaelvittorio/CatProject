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
// Import Wajib untuk Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.* // Khusus icon chat/inbox:
import androidx.compose.material.icons.outlined.MarkChatUnread // Khusus icon chat/inbox:

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
                        letterSpacing = (-1).sp
                    )
                },
                actions = {
                    // ICON INBOX (CHAT LIST)
                    IconButton(onClick = { navController.navigate("chat_list") }) {
                        Icon(Icons.Outlined.Send, contentDescription = "Messages") // Icon Pesawat Kertas khas DM
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.fillMaxSize()) {
            items(posts) { post ->
                // PERUBAHAN: allowDelete = false agar tidak bisa hapus dari Home
                PostItem(post, navController, allowDelete = false)
            }
        }
    }
}