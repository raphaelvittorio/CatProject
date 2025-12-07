package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.ChatUserItem
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    var chatList by remember { mutableStateOf<List<ChatUserItem>>(emptyList()) }
    val myId = UserSession.currentUser?.id ?: 0

    // Load Inbox saat dibuka
    LaunchedEffect(Unit) {
        try { chatList = RetrofitClient.instance.getChatList(myId) }
        catch (e: Exception) { e.printStackTrace() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        LazyColumn(
            contentPadding = p,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            items(chatList) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            // Pindah ke Room Chat
                            navController.navigate("chat_detail/${user.id}/${user.username}")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pp = if(user.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${user.profile_picture_url}" else "https://via.placeholder.com/150"

                    Image(
                        painter = rememberAsyncImagePainter(pp),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(user.last_message ?: "", color = Color.Gray, maxLines = 1, fontSize = 14.sp)
                    }
                }
                Divider(color = Color(0xFFF0F0F0))
            }
        }
    }
}