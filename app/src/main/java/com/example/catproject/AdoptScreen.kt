package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
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
import com.example.catproject.network.AdoptionPost
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptScreen(navController: NavController) {
    var adoptList by remember { mutableStateOf<List<AdoptionPost>>(emptyList()) }

    // Refresh saat layar dibuka
    LaunchedEffect(Unit) {
        try { adoptList = RetrofitClient.instance.getAdoptions() } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Adopt a Friend", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_adopt") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Adopt")
            }
        }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.padding(16.dp)) {
            items(adoptList) { item ->
                AdoptCard(item)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AdoptCard(post: AdoptionPost) {
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Foto Kucing
            Box(Modifier.height(220.dp).fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(baseUrl + post.image_url),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Badge Available
                Surface(
                    color = Color(0xFFE0F7FA),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd)
                ) {
                    Text("Available", color = Color(0xFF006064), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.cat_name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))

                // Owner Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val pp = if(post.profile_picture_url != null) baseUrl + post.profile_picture_url else "https://via.placeholder.com/50"
                    Image(rememberAsyncImagePainter(pp), null, Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(8.dp))
                    Text("Listed by ${post.username}", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(12.dp))
                Text(post.description, color = Color.DarkGray, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))

                // Tombol Kontak
                Button(
                    onClick = { /* Nanti bisa tambah Intent ke WhatsApp */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Orange
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Contact: ${post.contact_info}")
                }
            }
        }
    }
}