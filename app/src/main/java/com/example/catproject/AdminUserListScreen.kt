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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.AdminDeleteUserRequest
import com.example.catproject.network.AdminUpdateRoleRequest
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val myId = UserSession.currentUser?.id ?: 0

    // Fungsi Load Data
    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                // Mengambil daftar user dari server
                users = RetrofitClient.instance.adminGetUsers(myId)
            } catch (e: Exception) {
                Toast.makeText(context, "Connection Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Load data saat layar pertama kali dibuka
    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA) // Background abu-abu muda
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (users.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Text("No users found.", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = p) {
                items(users) { user ->
                    UserAdminCard(user, myId) {
                        loadData() // Refresh list setelah aksi (delete/update role)
                    }
                }
            }
        }
    }
}

@Composable
fun UserAdminCard(user: User, myId: Int, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdmin = user.role == "admin"

    // URL Gambar Profil (Gunakan placeholder jika null)
    val ppUrl = if (!user.profile_picture_url.isNullOrEmpty())
        "https://catpaw.my.id/catpaw_api/uploads/${user.profile_picture_url}"
    else "https://via.placeholder.com/150"

    // State untuk Dialog Konfirmasi Delete
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- DIALOG KONFIRMASI DELETE ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User?") },
            text = { Text("Are you sure you want to delete account '${user.username}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                // Panggil API Delete
                                val res = RetrofitClient.instance.adminDeleteUser(AdminDeleteUserRequest(user.id))
                                if (res.status == "success") {
                                    Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                                    onRefresh() // Refresh UI
                                } else {
                                    Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

    // --- ITEM CARD UI ---
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. FOTO PROFIL
            Image(
                painter = rememberAsyncImagePainter(ppUrl),
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // 2. INFO USER
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))

                    // Badge Role
                    Surface(
                        color = if (isAdmin) Color(0xFFE3F2FD) else Color(0xFFEEEEEE),
                        contentColor = if (isAdmin) Color(0xFF1976D2) else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(user.email, color = Color.Gray, fontSize = 12.sp)
            }

            // 3. ACTION BUTTONS
            Row {
                // Logic: Jangan tampilkan tombol aksi untuk diri sendiri
                if (user.id != myId) {
                    // Tombol Ganti Role
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val newRole = if (isAdmin) "user" else "admin"
                                val res = RetrofitClient.instance.adminUpdateRole(AdminUpdateRoleRequest(user.id, newRole))
                                if (res.status == "success") {
                                    onRefresh()
                                    Toast.makeText(context, "Role updated to $newRole", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error updating role", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if(isAdmin) Icons.Rounded.Shield else Icons.Rounded.Security,
                            contentDescription = "Change Role",
                            tint = if(isAdmin) Color(0xFF1976D2) else Color.Gray
                        )
                    }

                    // Tombol Hapus (Trigger Dialog)
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = "Delete", tint = Color.Red)
                    }
                } else {
                    // Penanda akun sendiri
                    Text(
                        "(You)",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
                    )
                }
            }
        }
    }
}