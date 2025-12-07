package com.example.catproject

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.catproject.network.ProfileResponse
import com.example.catproject.network.RetrofitClient

@Composable
fun ProfileScreen() {
    var data by remember { mutableStateOf<ProfileResponse?>(null) }
    LaunchedEffect(Unit) {
        UserSession.currentUser?.let { data = RetrofitClient.instance.getProfile(it.id) }
    }

    if (data != null) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val pp = if(data!!.user.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${data!!.user.profile_picture_url}" else "https://via.placeholder.com/150"
                AsyncImage(model = pp, contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), contentScale = ContentScale.Crop)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("Posts\n${data!!.stats.posts}")
                    Text("Followers\n${data!!.stats.followers}")
                }
            }
            Text(text = data!!.user.username, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)
            Text(text = data!!.user.bio ?: "", modifier = Modifier.padding(start = 16.dp, bottom = 16.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                items(data!!.posts) { post ->
                    AsyncImage(model = "http://10.0.2.2/catpaw_api/uploads/${post.image_url}", contentDescription = null, modifier = Modifier.aspectRatio(1f).padding(1.dp), contentScale = ContentScale.Crop)
                }
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}