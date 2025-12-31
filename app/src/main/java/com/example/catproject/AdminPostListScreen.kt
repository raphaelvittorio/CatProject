package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.DeletePostRequest
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPostListScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val myId = UserSession.currentUser?.id ?: 0

    LaunchedEffect(Unit) {
        try {
            posts = RetrofitClient.instance.adminGetAllPosts()
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading posts", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Content Moderation", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF9800)) }
        } else {
            LazyColumn(
                contentPadding = p,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(posts) { post ->
                    AdminPostCard(post, myId) {
                        // Refresh logic manual (hapus dari list lokal agar cepat)
                        posts = posts.filter { it.id != post.id }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun AdminPostCard(post: Post, myId: Int, onDeleteSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // 1. HEADER (User Info)
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pp = if (!post.profile_picture_url.isNullOrEmpty()) "https://catpaw.my.id/catpaw_api/uploads/${post.profile_picture_url}" else "https://via.placeholder.com/150"
                Image(
                    painter = rememberAsyncImagePainter(pp),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // DELETE BUTTON (Distinctive)
                IconButton(
                    onClick = {
                        if (!isDeleting) {
                            isDeleting = true
                            scope.launch {
                                try {
                                    val res = RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                    if (res.status == "success") {
                                        Toast.makeText(context, "Post deleted permanently", Toast.LENGTH_SHORT).show()
                                        onDeleteSuccess()
                                    } else {
                                        isDeleting = false
                                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isDeleting = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Red, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // 2. IMAGE CONTENT
            Image(
                painter = rememberAsyncImagePainter("https://catpaw.my.id/catpaw_api/uploads/${post.image_url}"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            // 3. CAPTION
            if (!post.caption.isNullOrEmpty()) {
                Text(
                    text = post.caption,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}