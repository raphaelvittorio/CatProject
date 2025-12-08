package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.example.catproject.network.EventPost
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(navController: NavController) {
    var eventList by remember { mutableStateOf<List<EventPost>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Fungsi refresh
    fun loadEvents() {
        scope.launch {
            try { eventList = RetrofitClient.instance.getEvents() } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) { loadEvents() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Community Events", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        // --- TOMBOL BUAT EVENT (BARU) ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_event") },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        }
    ) { p ->
        LazyColumn(
            contentPadding = p,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            items(eventList) { event ->
                EventCard(event)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun EventCard(event: EventPost) {
    val imageUrl = if (!event.image_url.isNullOrEmpty())
        "http://10.0.2.2/catpaw_api/uploads/${event.image_url}"
    else "https://via.placeholder.com/400x200"

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.event_date, fontSize = 14.sp, color = Color.Gray)

                    Spacer(Modifier.width(16.dp))

                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.time, fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.location, fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Join Event", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}