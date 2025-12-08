package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.NotificationItem
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    val myId = UserSession.currentUser?.id ?: 0
    val scope = rememberCoroutineScope()

    // Load data
    LaunchedEffect(Unit) {
        scope.launch {
            try { notifications = RetrofitClient.instance.getNotifications(myId) }
            catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications yet", color = Color.Gray)
                    }
                }
            } else {
                items(notifications) { notif ->
                    NotificationItemRow(notif)
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(notif: NotificationItem) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (notif.actor_pic != null) baseUrl + notif.actor_pic else "https://via.placeholder.com/150"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Avatar Actor
        Image(
            painter = rememberAsyncImagePainter(pp),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Text (Username + Action)
        val text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(notif.actor_name) }
            append(" ")
            when (notif.type) {
                "like" -> append("liked your post.")
                "comment" -> append("commented: Nice cat!") // Simplify text for now
                "follow" -> append("started following you.")
                else -> append("interacted with you.")
            }
            append(" ")
            withStyle(SpanStyle(color = Color.Gray, fontSize = 12.sp)) {
                // Simple date parse or just raw
                append(notif.created_at)
            }
        }

        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 3. Right Side Content (Post Image or Follow Button)
        if (notif.type == "follow") {
            Button(
                onClick = { /* Logic follow back if needed */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("Follow", fontSize = 12.sp)
            }
        } else if (notif.post_image != null) {
            Image(
                painter = rememberAsyncImagePainter(baseUrl + notif.post_image),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}