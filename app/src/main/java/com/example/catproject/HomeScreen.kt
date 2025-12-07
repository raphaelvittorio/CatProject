package com.example.catproject
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val myId = UserSession.currentUser?.id ?: 0
    LaunchedEffect(Unit) { try { posts = RetrofitClient.instance.getPosts(myId) } catch (e: Exception) {} }

    Scaffold(topBar = {
        TopAppBar(title = { Text("CatPaw", fontFamily = FontFamily.Cursive, fontSize = 28.sp) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
    }) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.background(Color.White)) {
            item { LazyRow(contentPadding = PaddingValues(10.dp)) { item { StoryItem("", "You", true) }; items(5) { StoryItem("https://placekitten.com/100/100", "Cat $it") } }; Divider() }
            items(posts) { post -> PostItem(post, navController) }
        }
    }
}

@Composable
fun PostItem(post: Post, navController: NavController) {
    var isLiked by remember { mutableStateOf(post.is_liked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    val scope = rememberCoroutineScope()
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val pp = if (post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

    Column(Modifier.padding(bottom = 12.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(rememberAsyncImagePainter(pp), null, Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp)); Text(post.username, fontWeight = FontWeight.Bold)
        }
        Image(rememberAsyncImagePainter(baseUrl + post.image_url), null, Modifier.fillMaxWidth().aspectRatio(1f), contentScale = ContentScale.Crop)
        Row(Modifier.padding(10.dp)) {
            Icon(if(isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null,
                tint = if(isLiked) Color.Red else Color.Black,
                modifier = Modifier.size(28.dp).clickable {
                    isLiked = !isLiked; likeCount += if(isLiked) 1 else -1
                    scope.launch { RetrofitClient.instance.toggleLike(LikeRequest(UserSession.currentUser?.id?:0, post.id)) }
                })
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(28.dp).clickable { navController.navigate("comments/${post.id}") })
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.Send, null, Modifier.size(28.dp))
        }
        Column(Modifier.padding(horizontal = 12.dp)) {
            Text("$likeCount likes", fontWeight = FontWeight.Bold)
            Text("${post.username} ${post.caption}")
            Text("View all comments", color = Color.Gray, modifier = Modifier.clickable { navController.navigate("comments/${post.id}") })
        }
    }
}