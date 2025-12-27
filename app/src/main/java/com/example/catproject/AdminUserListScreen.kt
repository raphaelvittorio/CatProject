package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.AdminDeleteUserRequest
import com.example.catproject.network.AdminUpdateRoleRequest
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val myId = UserSession.currentUser?.id ?: 0

    // Load Users
    LaunchedEffect(Unit) {
        try { users = RetrofitClient.instance.adminGetUsers(myId) } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBack, null) } }
            )
        }
    ) { p ->
        LazyColumn(contentPadding = p) {
            items(users) { user ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.username, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            // Badge Role
                            Surface(
                                color = if(user.role == "admin") Color.Blue.copy(0.1f) else Color.Gray.copy(0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(user.role.uppercase(), modifier = Modifier.padding(4.dp), fontSize = 10.sp, color = Color.Black)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(user.email, color = Color.Gray, fontSize = 12.sp)

                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            // TOMBOL JADIKAN ADMIN / USER
                            TextButton(onClick = {
                                scope.launch {
                                    val newRole = if (user.role == "admin") "user" else "admin"
                                    try {
                                        RetrofitClient.instance.adminUpdateRole(AdminUpdateRoleRequest(user.id, newRole))
                                        users = RetrofitClient.instance.adminGetUsers(myId) // Refresh
                                        Toast.makeText(context, "Role updated to $newRole", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {}
                                }
                            }) {
                                Icon(Icons.Rounded.Security, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (user.role == "admin") "Demote" else "Make Admin")
                            }

                            Spacer(Modifier.width(8.dp))

                            // TOMBOL DELETE USER
                            TextButton(onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.instance.adminDeleteUser(AdminDeleteUserRequest(user.id))
                                        users = RetrofitClient.instance.adminGetUsers(myId) // Refresh
                                        Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {}
                                }
                            }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                                Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(16.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}