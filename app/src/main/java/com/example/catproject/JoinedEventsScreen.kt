package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.catproject.network.Event
import com.example.catproject.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinedEventsScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val myId = UserSession.currentUser?.id ?: 0

    LaunchedEffect(Unit) {
        try {
            events = RetrofitClient.instance.getJoinedEvents(myId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Joined Events", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (events.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't joined any events yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = p, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(events) { event ->
                    // Kita bisa pakai Card yang lebih simpel atau reuse
                    JoinedEventCard(event)
                }
            }
        }
    }
}

@Composable
fun JoinedEventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${event.image_url}"),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(event.event_date, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(event.location, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Text("You are going!", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}