package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.GridPost
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController) {
    var explorePosts by remember { mutableStateOf<List<GridPost>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { explorePosts = RetrofitClient.instance.getExplore() } catch (e: Exception) {}
    }

    fun doSearch(query: String) {
        searchQuery = query
        if (query.isNotEmpty()) {
            scope.launch {
                try { searchResults = RetrofitClient.instance.searchUsers(query) }
                catch (e: Exception) {}
            }
        } else { searchResults = emptyList() }
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        // MODERN SEARCH BAR
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = { doSearch(it) },
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0), // Abu Muda
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent, // Hapus garis bawah
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp), // Sudut tumpul
                singleLine = true
            )
        }

        if (searchQuery.isNotEmpty()) {
            // HASIL SEARCH
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(searchResults) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clickable { navController.navigate("visit_profile/${user.id}") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pp = if(user.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${user.profile_picture_url}" else "https://via.placeholder.com/150"
                        Image(
                            painter = rememberAsyncImagePainter(pp),
                            contentDescription = null,
                            modifier = Modifier.size(54.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if(!user.bio.isNullOrEmpty()) {
                                Text(user.bio, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        } else {
            // GRID FOTO
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(explorePosts) { post ->
                    Image(
                        painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${post.image_url}"),
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { navController.navigate("post_detail/${post.id}") },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}