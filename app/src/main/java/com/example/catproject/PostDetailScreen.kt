package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, postId: Int) {
    var post by remember { mutableStateOf<Post?>(null) }
    val myId = UserSession.currentUser?.id ?: 0

    LaunchedEffect(postId) {
        try { post = RetrofitClient.instance.getPostDetail(postId, myId) } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (post != null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // PERUBAHAN: allowDelete = true.
                    // Tombol hapus akan muncul DI SINI jika ini post milik user.
                    PostItem(post = post!!, navController = navController, allowDelete = true)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}