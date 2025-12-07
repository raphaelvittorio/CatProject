package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    // Hardcode stories dummy untuk visual
    val stories = listOf("kucing1", "si_meong", "orange_cat", "admin", "cat_lover")

    LaunchedEffect(Unit) { try { posts = RetrofitClient.instance.getPosts() } catch (e: Exception) {} }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("CatPaw", fontFamily = FontFamily.Cursive, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(26.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Outlined.Send, null, modifier = Modifier.size(26.dp)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.fillMaxSize().background(Color.White)) {
            // --- SECTION STORIES ---
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item { StoryItem("https://via.placeholder.com/150", "Your Story", true) }
                    items(stories) { name ->
                        StoryItem("https://placekitten.com/100/100", name)
                    }
                }
                Divider(thickness = 0.5.dp, color = Color.LightGray)
            }

            // --- SECTION POSTS ---
            items(posts) { post -> PostItem(post) }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        // Header
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = rememberAsyncImagePainter(pp), contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp))
            Text(post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(20.dp))
        }

        // Image (Square)
        Image(
            painter = rememberAsyncImagePainter(baseUrl + post.image_url),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale = ContentScale.Crop
        )

        // Actions
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.Send, null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(28.dp))
        }

        // Caption & Likes
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text("1,234 likes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            val text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(post.username + " ") }
                append(post.caption ?: "")
            }
            Text(text, fontSize = 14.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("View all comments", color = Color.Gray, fontSize = 14.sp)
            Text("2 hours ago", color = Color.Gray, fontSize = 10.sp)
        }
    }
}