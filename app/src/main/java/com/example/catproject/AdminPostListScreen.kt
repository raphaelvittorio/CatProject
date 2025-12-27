package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val myId = UserSession.currentUser?.id ?: 0

    LaunchedEffect(Unit) {
        try { posts = RetrofitClient.instance.adminGetAllPosts() } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Posts") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBack, null) } }
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p) {
            items(posts) { post ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${post.image_url}"),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(post.caption ?: "", maxLines = 1, fontSize = 13.sp, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    // Admin bisa hapus, kirim ID Admin sbg user_id
                                    val res = RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                    if(res.status == "success") {
                                        posts = RetrofitClient.instance.adminGetAllPosts()
                                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {}
                            }
                        }) {
                            Icon(Icons.Rounded.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}