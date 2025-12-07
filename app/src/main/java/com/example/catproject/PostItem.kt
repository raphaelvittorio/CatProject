package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.DeletePostRequest
import com.example.catproject.network.LikeRequest
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun PostItem(
    post: Post,
    navController: NavController,
    allowDelete: Boolean = false // PARAMETER BARU (Default False)
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State UI
    var isLiked by remember { mutableStateOf(post.is_liked) }
    var likeCount by remember { mutableStateOf(post.like_count) }

    // State Menu Dropdown
    var showMenu by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }

    // Cek apakah ini post milik saya?
    val myId = UserSession.currentUser?.id ?: 0
    val isOwner = (post.user_id == myId)

    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

    if (!isDeleted) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar & Username
                Row(
                    modifier = Modifier.weight(1f).clickable {
                        navController.navigate("visit_profile/${post.user_id}")
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(pp),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(post.username, fontWeight = FontWeight.Bold)
                }

                // --- UPDATE LOGIC DELETE ---
                // Hanya muncul jika: Pemilik Postingan DAN Diizinkan (allowDelete = true)
                if (isOwner && allowDelete) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                            if (res.status == "success") {
                                                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                                isDeleted = true
                                                // Jika di halaman detail, kembali ke profil
                                                if (allowDelete) navController.popBackStack()
                                            } else {
                                                Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // --- IMAGE ---
            Image(
                painter = rememberAsyncImagePainter(baseUrl + post.image_url),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            // --- ACTIONS ---
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color.Red else Color.Black,
                    modifier = Modifier.size(28.dp).clickable {
                        isLiked = !isLiked
                        likeCount += if (isLiked) 1 else -1
                        scope.launch {
                            RetrofitClient.instance.toggleLike(LikeRequest(myId, post.id))
                        }
                    }
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp).clickable {
                        navController.navigate("comments/${post.id}")
                    }
                )
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Outlined.Send, null, modifier = Modifier.size(28.dp))
            }

            // --- CAPTION ---
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("$likeCount likes", fontWeight = FontWeight.Bold)
                Row {
                    Text(post.username, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = post.caption ?: "")
                }
                Text(
                    "View all comments",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        navController.navigate("comments/${post.id}")
                    }
                )
            }
        }
    }
}