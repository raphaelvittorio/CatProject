package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.catproject.network.ProfileResponse
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var data by remember { mutableStateOf<ProfileResponse?>(null) }
    LaunchedEffect(Unit) {
        UserSession.currentUser?.let { data = RetrofitClient.instance.getProfile(it.id) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(data?.user?.username ?: "Loading...", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, null) } }
            )
        }
    ) { p ->
        if (data != null) {
            Column(Modifier.padding(p).fillMaxSize().background(Color.White)) {
                // Header
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val pp = if(data!!.user.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${data!!.user.profile_picture_url}" else "https://via.placeholder.com/150"
                    AsyncImage(model = pp, contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape).border(1.dp, Color.LightGray, CircleShape), contentScale = ContentScale.Crop)
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(data!!.stats.posts.toString(), "Posts")
                        StatItem("890", "Followers")
                        StatItem("120", "Following")
                    }
                }

                // Bio
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(data!!.user.username, fontWeight = FontWeight.Bold)
                    Text(data!!.user.bio ?: "Cat lover from Indonesia 🇮🇩")
                }

                // Buttons
                Row(Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Text("Edit profile", color = Color.Black)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                        Text("Share profile", color = Color.Black)
                    }
                }

                // Tabs
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Icon(Icons.Default.GridOn, null, modifier = Modifier.size(28.dp))
                    Icon(Icons.Outlined.AccountBox, null, modifier = Modifier.size(28.dp), tint = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))

                // Grid
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    items(data!!.posts) { post ->
                        AsyncImage(
                            model = "http://10.0.2.2/catpaw_api/uploads/${post.image_url}",
                            contentDescription = null,
                            modifier = Modifier.aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 13.sp)
    }
}