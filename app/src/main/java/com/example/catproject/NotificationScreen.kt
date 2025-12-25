package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val myId = UserSession.currentUser?.id ?: 0
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load Data saat halaman dibuka
    LaunchedEffect(Unit) {
        try {
            val result = RetrofitClient.instance.getNotifications(myId)
            notifications = result
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            // Tampilkan error jika gagal load
            Toast.makeText(context, "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = p,
                modifier = Modifier.fillMaxSize().background(Color.White)
            ) {
                items(notifications) { item ->
                    NotificationItemRow(item, navController, myId)
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(item: NotificationItem, navController: NavController, myId: Int) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val actorPic = if (!item.actor_pic.isNullOrEmpty()) baseUrl + item.actor_pic else "https://via.placeholder.com/150"

    // STATE LOKAL: Diinisialisasi dengan data dari Server (is_following)
    // Jadi saat masuk lagi, statusnya sesuai database
    var isFollowingState by remember { mutableStateOf(item.is_following == true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("visit_profile/${item.actor_id}")
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto Profil Actor
        Image(
            painter = rememberAsyncImagePainter(actorPic),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Teks Notifikasi
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.actor_name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            val message = when (item.type) {
                "like" -> "liked your post."
                "comment" -> "commented: \"${item.comment_text ?: "..."}\""
                "follow" -> "started following you."
                else -> "interacted with you."
            }

            Text(text = message, color = Color.Gray, fontSize = 13.sp)
        }

        // --- TOMBOL FOLLOW (Hanya jika tipe notifikasi 'follow') ---
        if (item.type == "follow") {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            // Panggil API Toggle Follow
                            val req = FollowRequest(follower_id = myId, following_id = item.actor_id)
                            RetrofitClient.instance.toggleFollow(req)

                            // Ubah tampilan tombol secara instan
                            isFollowingState = !isFollowingState
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp),
                // LOGIKA WARNA: Abu-abu jika sudah follow, Orange jika belum
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowingState) Color(0xFFE0E0E0) else Color(0xFFFF9800),
                    contentColor = if (isFollowingState) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (isFollowingState) "Followed" else "Follow Back",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // --- THUMBNAIL POST (Jika tipe notifikasi 'like'/'comment') ---
        else if (!item.post_image.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(baseUrl + item.post_image),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}