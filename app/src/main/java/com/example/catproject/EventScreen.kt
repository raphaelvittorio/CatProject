package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(navController: NavController) {
    // Gunakan model Event yang sudah ada di ApiService (sesuaikan nama classnya jika berbeda)
    // Asumsi: di ApiService nama classnya adalah "Event" (seperti di panduan sebelumnya)
    // Jika di ApiService Anda pakai "EventPost", ubah "Event" jadi "EventPost" di bawah ini.
    var eventList by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Ambil user saat ini
    val currentUser = UserSession.currentUser
    val myId = currentUser?.id ?: 0

    // Fungsi refresh
    fun loadEvents() {
        scope.launch {
            try {
                // Panggil endpoint getEvents(userId) agar status is_joined terisi
                eventList = RetrofitClient.instance.getEvents(myId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
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
        floatingActionButton = {
            // --- LOGIKA ADMIN / CREATOR ---
            // Jika ingin semua user bisa buat event, hapus if-nya.
            // Sesuai kode Anda sebelumnya, hanya admin:
            if (currentUser?.role == "admin") {
                FloatingActionButton(
                    onClick = { navController.navigate("create_event") },
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Event")
                }
            }
        }
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (eventList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Text("No upcoming events", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = p,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }

                items(eventList) { event ->
                    EventCard(event, myId, onRefresh = { loadEvents() })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, myId: Int, onRefresh: () -> Unit) {
    // Format URL
    val imageUrl = if (!event.image_url.isNullOrEmpty())"https://catpaw.my.id/catpaw_api/uploads/${event.image_url}"
    else "https://via.placeholder.com/400x200"

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Status Kepemilikan & Join
    val isOwner = (event.user_id == myId)
    var isJoined by remember { mutableStateOf(event.is_joined) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to cancel this event?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.deleteEvent(DeleteEventRequest(event.id, myId))
                                if (res.status == "success") {
                                    Toast.makeText(context, "Event Deleted", Toast.LENGTH_SHORT).show()
                                    onRefresh()
                                } else {
                                    Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- GAMBAR EVENT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Tombol Delete (Hanya Owner/Admin)
                if (isOwner) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // --- KONTEN EVENT ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Spacer(Modifier.height(12.dp))

                // Baris Tanggal & Waktu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.event_date, fontSize = 14.sp, color = Color.Gray)

                    Spacer(Modifier.width(16.dp))

                    Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.time, fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))

                // Baris Lokasi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text(event.location, fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(12.dp))

                // Deskripsi
                Text(text = event.description, fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray)

                Spacer(Modifier.height(16.dp))

                // --- TOMBOL JOIN / JOINED ---
                Button(
                    onClick = {
                        val oldState = isJoined
                        isJoined = !isJoined // Toggle UI instant

                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.toggleEventJoin(EventJoinRequest(myId, event.id))
                                val msg = if (res.action == "joined") "You joined the event!" else "You left the event."
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                isJoined = oldState // Rollback jika error
                                Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJoined) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isJoined) {
                        Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Joined", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Join Event", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}