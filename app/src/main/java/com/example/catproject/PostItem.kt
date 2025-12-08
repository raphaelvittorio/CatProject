package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
// IMPORT IKON MODERN (OUTLINED)
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Delete
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
import com.example.catproject.network.DeletePostRequest
import com.example.catproject.network.LikeRequest
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun PostItem(
    post: Post,
    navController: NavController,
    allowDelete: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLiked by remember { mutableStateOf(post.is_liked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    var showMenu by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }

    val myId = UserSession.currentUser?.id ?: 0
    val isOwner = (post.user_id == myId)
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

    if (!isDeleted) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).clickable { navController.navigate("visit_profile/${post.user_id}") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(pp),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(post.username, fontWeight = FontWeight.Bold)
                }

                if (isOwner && allowDelete) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                            Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                            isDeleted = true
                                            if (allowDelete) navController.popBackStack()
                                        } catch (e: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // IMAGE
            Image(
                painter = rememberAsyncImagePainter(baseUrl + post.image_url),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            // ACTIONS (IKON BARU: Outlined semua)
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                // Like: Jika liked pakai Filled (Merah), jika tidak pakai Outlined (Hitam Tipis)
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color(0xFFFF3D00) else Color.Black, // Merah Cerah vs Hitam
                    modifier = Modifier.size(26.dp).clickable {
                        isLiked = !isLiked
                        likeCount += if (isLiked) 1 else -1
                        scope.launch { RetrofitClient.instance.toggleLike(LikeRequest(myId, post.id)) }
                    }
                )
                Spacer(Modifier.width(16.dp))

                // Comment: Outlined Bubble
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp).clickable { navController.navigate("comments/${post.id}") }
                )
                Spacer(Modifier.width(16.dp))

                // Share: Outlined Paper Plane
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }

            // CAPTION
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("$likeCount likes", fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(post.username, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = post.caption ?: "")
                }
                Spacer(Modifier.height(4.dp))
                Text("View all comments", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.clickable { navController.navigate("comments/${post.id}") })
            }
        }
    }
}