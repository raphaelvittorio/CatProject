package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    LaunchedEffect(Unit) { try { posts = RetrofitClient.instance.getPosts() } catch (e: Exception) {} }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("CatPaw Feed", fontWeight = FontWeight.Bold) }) }) { p ->
        LazyColumn(contentPadding = p) {
            items(posts) { post -> PostItem(post) }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = rememberAsyncImagePainter(pp), contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(8.dp))
            Text(post.username, fontWeight = FontWeight.Bold)
        }
        Image(painter = rememberAsyncImagePainter(baseUrl + post.image_url), contentDescription = null, modifier = Modifier.fillMaxWidth().height(400.dp), contentScale = ContentScale.Crop)
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.FavoriteBorder, null); Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.ChatBubbleOutline, null); Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.Send, null)
        }
        Text(text = "${post.username}: ${post.caption}", modifier = Modifier.padding(horizontal = 12.dp))
    }
}