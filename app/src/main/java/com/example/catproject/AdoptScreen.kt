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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.AdoptionPost
import com.example.catproject.network.DeleteAdoptionRequest
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptScreen(navController: NavController) {
    var adoptList by remember { mutableStateOf<List<AdoptionPost>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Fungsi load data
    fun loadAdoptions() {
        scope.launch {
            try { adoptList = RetrofitClient.instance.getAdoptions() } catch (e: Exception) {}
        }
    }

    // Refresh setiap kali halaman dibuka/ditampilkan
    LaunchedEffect(Unit) { loadAdoptions() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Find a Friend", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_adopt") },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "List Cat")
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

            items(adoptList) { item ->
                // Pass fungsi loadAdoptions agar list refresh setelah delete
                ModernAdoptCard(item, navController, onRefresh = { loadAdoptions() })
                Spacer(modifier = Modifier.height(20.dp))
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ModernAdoptCard(
    post: AdoptionPost,
    navController: NavController,
    onRefresh: () -> Unit
) {
    val baseUrl = "https://catpaw.my.id/catpaw_api/uploads/"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val myId = UserSession.currentUser?.id ?: 0
    val isOwner = (post.user_id == myId)
    val isAdopted = (post.status == "adopted")

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog Delete
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing?") },
            text = { Text("Remove ${post.cat_name} from list?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                RetrofitClient.instance.deleteAdoption(DeleteAdoptionRequest(post.id, myId))
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                onRefresh()
                            } catch (e: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- IMAGE ---
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(baseUrl + post.image_url),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 300f)))

                // POJOK KANAN ATAS (Delete / Status)
                if (isOwner) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) }
                } else {
                    // Badge Status
                    val badgeColor = if (isAdopted) Color.Gray else Color(0xFF00C853)
                    val badgeText = if (isAdopted) "ADOPTED" else "Available"

                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                    ) {
                        Text(badgeText, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }

                // Info Overlay
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(text = post.cat_name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "Listed by ${post.username}", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                }
            }

            // --- DETAILS ---
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "About", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text(text = post.description, color = Color.Gray, fontSize = 14.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(24.dp))

                // --- ACTION BUTTON ---
                if (isAdopted) {
                    // SUDAH DIADOPSI
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        enabled = false
                    ) {
                        Text("Already Adopted", color = Color.White)
                    }
                } else if (!isOwner) {
                    // TOMBOL APPLY (Utama)
                    Button(
                        onClick = {
                            navController.navigate("apply_adopt/${post.id}/${post.cat_name}")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(Icons.Default.Pets, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Apply to Adopt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    // OWNER VIEW
                    Text("This is your listing.", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}