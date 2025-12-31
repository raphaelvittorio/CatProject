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
import androidx.compose.material.icons.filled.Delete
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
import com.example.catproject.network.DeleteNotificationRequest
import com.example.catproject.network.FollowRequest
import com.example.catproject.network.NotificationItem
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    // Gunakan mutableStateListOf agar perubahan list (hapus item) terdeteksi UI secara langsung
    var notifications = remember { mutableStateListOf<NotificationItem>() }
    var isLoading by remember { mutableStateOf(true) }

    val myId = UserSession.currentUser?.id ?: 0
    val context = LocalContext.current

    // Load Data
    LaunchedEffect(Unit) {
        try {
            val result = RetrofitClient.instance.getNotifications(myId)
            notifications.clear()
            notifications.addAll(result)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = p,
                modifier = Modifier.fillMaxSize().background(Color.White)
            ) {
                // key sangat penting untuk animasi swipe agar tidak salah item
                items(items = notifications, key = { it.id }) { item ->
                    SwipableNotificationItem(
                        item = item,
                        navController = navController,
                        myId = myId,
                        onDelete = {
                            notifications.remove(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableNotificationItem(
    item: NotificationItem,
    navController: NavController,
    myId: Int,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    // State untuk Swipe
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                // Saat di-slide ke kiri, jangan langsung hapus, tapi munculkan dialog
                showDialog = true
                false // Return false agar swipe tidak langsung menghilangkan item secara visual
            } else {
                false
            }
        }
    )

    // Dialog Konfirmasi
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Notification?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        // Eksekusi Hapus ke Server
                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.deleteNotification(
                                    DeleteNotificationRequest(item.id, myId)
                                )
                                if (res.status == "success") {
                                    onDelete() // Hapus dari UI List
                                    Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    // Reset swipe state jika batal
                    scope.launch { dismissState.reset() }
                }) { Text("Cancel") }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false, // Matikan swipe kanan
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = {
            // Konten NotificationItemRow asli, dibungkus Background Putih agar tidak transparan saat swipe
            Box(modifier = Modifier.background(Color.White)) {
                NotificationItemRow(item, navController, myId)
            }
        }
    )
}

@Composable
fun NotificationItemRow(item: NotificationItem, navController: NavController, myId: Int) {
    val baseUrl = "https://catpaw.my.id/catpaw_api/uploads/"
    val actorPic = if (!item.actor_pic.isNullOrEmpty()) baseUrl + item.actor_pic else "https://via.placeholder.com/150"

    var isFollowing by remember { mutableStateOf(item.is_following == true) }
    var isProcessing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("visit_profile/${item.actor_id}") }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. FOTO PROFIL
        Image(
            painter = rememberAsyncImagePainter(actorPic),
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 2. TEXT NOTIFIKASI
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(item.actor_name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = when (item.type) {
                        "like" -> "liked your post."
                        "comment" -> "commented on your post."
                        "follow" -> "started following you."
                        else -> "interacted with you."
                    },
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Text(
                text = item.created_at,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // 3. ACTION BUTTON (Follow Back / Post Thumbnail)
        if (item.type == "follow") {
            Button(
                onClick = {
                    if (!isProcessing) {
                        isProcessing = true
                        val newState = !isFollowing
                        isFollowing = newState

                        scope.launch {
                            try {
                                RetrofitClient.instance.toggleFollow(FollowRequest(myId, item.actor_id))
                            } catch (e: Exception) {
                                isFollowing = !newState
                                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                },
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color(0xFFE0E0E0) else Color(0xFFFF9800),
                    contentColor = if (isFollowing) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow Back",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (!item.post_image.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(baseUrl + item.post_image),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}