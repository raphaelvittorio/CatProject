package com.example.catproject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var storyUsers by remember { mutableStateOf<List<StoryUser>>(emptyList()) }
    val myId = UserSession.currentUser?.id ?: 0
    val scope = rememberCoroutineScope()
    val amIHaveStory = storyUsers.any { it.id == myId }

    LaunchedEffect(Unit) {
        scope.launch {
            try { posts = RetrofitClient.instance.getPosts(myId) } catch (e: Exception) {}
            try { storyUsers = RetrofitClient.instance.getActiveStories() } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CatPaw",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color(0xFFFF9800)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("add") }) {
                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp), tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Outlined.FavoriteBorder, "Notif", modifier = Modifier.size(26.dp), tint = Color.Black)
                    }
                    IconButton(onClick = { navController.navigate("chat_list") }) {
                        Icon(Icons.AutoMirrored.Outlined.Send, "Chat", modifier = Modifier.size(26.dp), tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.fillMaxSize().background(Color.White)) {

            // --- STORY SECTION ---
            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        StoryCircle(
                            name = "Your story",
                            imageUrl = UserSession.currentUser?.profile_picture_url,
                            isMe = true,
                            hasStory = amIHaveStory,
                            onClick = {
                                if (amIHaveStory) navController.navigate("story_view/$myId")
                                else navController.navigate("add")
                            }
                        )
                    }
                    items(storyUsers) { user ->
                        if (user.id != myId) {
                            StoryCircle(
                                name = user.username,
                                imageUrl = user.profile_picture_url,
                                isMe = false,
                                hasStory = true,
                                onClick = { navController.navigate("story_view/${user.id}") }
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            }

            // --- POST SECTION ---
            items(posts) { post ->
                PostItem(post, navController)
            }
        }
    }
}

@Composable
fun StoryCircle(name: String, imageUrl: String?, isMe: Boolean, hasStory: Boolean, onClick: () -> Unit) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pic = if (!imageUrl.isNullOrEmpty()) baseUrl + imageUrl else "https://via.placeholder.com/150"

    val borderBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFF9CE34), Color(0xFFEE2A7B), Color(0xFF6228D7))
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(
                        if (hasStory) BorderStroke(2.5.dp, borderBrush) else BorderStroke(0.dp, Color.Transparent),
                        CircleShape
                    )
                    .padding(5.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(pic),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { onClick() },
                    contentScale = ContentScale.Crop
                )
            }
            if (isMe && !hasStory) {
                Box(
                    modifier = Modifier.size(24.dp).background(Color.White, CircleShape).padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize().background(Color(0xFF0095F6), CircleShape)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if(hasStory) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
    }
}