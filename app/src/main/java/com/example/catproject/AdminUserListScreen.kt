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

    // Load Users
    LaunchedEffect(Unit) {
        try {
            users = RetrofitClient.instance.adminGetUsers(myId)
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading users", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF9800)) }
        } else {
            LazyColumn(contentPadding = p) {
                items(users) { user ->
                    UserAdminCard(user, myId) {
                        // Refresh Callback
                        scope.launch { try { users = RetrofitClient.instance.adminGetUsers(myId) } catch (e: Exception){} }
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
    val ppUrl = if (!user.profile_picture_url.isNullOrEmpty()) "http://10.0.2.2/catpaw_api/uploads/${user.profile_picture_url}" else "https://via.placeholder.com/150"

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. AVATAR
            Image(
                painter = rememberAsyncImagePainter(ppUrl),
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // 2. INFO
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    // ROLE BADGE
                    Surface(
                        color = if (isAdmin) Color(0xFFE3F2FD) else Color(0xFFEEEEEE),
                        contentColor = if (isAdmin) Color(0xFF1976D2) else Color.Gray,
                        shape = RoundedCornerShape(6.dp)
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

            // 3. ACTIONS
            Row {
                // Role Button
                IconButton(onClick = {
                    scope.launch {
                        val newRole = if (isAdmin) "user" else "admin"
                        RetrofitClient.instance.adminUpdateRole(AdminUpdateRoleRequest(user.id, newRole))
                        onRefresh()
                        Toast.makeText(context, "Role updated", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = if(isAdmin) Icons.Rounded.Shield else Icons.Rounded.Security,
                        contentDescription = "Role",
                        tint = if(isAdmin) Color(0xFF1976D2) else Color.Gray
                    )
                }

                // Delete Button
                IconButton(onClick = {
                    scope.launch {
                        RetrofitClient.instance.adminDeleteUser(AdminDeleteUserRequest(user.id))
                        onRefresh()
                        Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}