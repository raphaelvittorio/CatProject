package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import com.example.catproject.network.Comment
import com.example.catproject.network.CommentRequest
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController, // Tambahkan parameter ini
    postId: Int
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load Komentar
    fun loadComments() {
        scope.launch {
            try {
                comments = RetrofitClient.instance.getComments(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) { loadComments() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // TOMBOL BACK
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            // LIST KOMENTAR
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(comments) { item ->
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        val pp = if(item.profile_picture_url != null)
                            "http://10.0.2.2/catpaw_api/uploads/${item.profile_picture_url}"
                        else "https://via.placeholder.com/150"

                        Image(
                            painter = rememberAsyncImagePainter(pp),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(item.username, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.comment, fontSize = 13.sp)
                        }
                    }
                }
            }

            // INPUT KOMENTAR
            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (newComment.isNotEmpty() && UserSession.currentUser != null) {
                        scope.launch {
                            val req = CommentRequest(UserSession.currentUser!!.id, postId, newComment)
                            val res = RetrofitClient.instance.addComment(req)
                            if (res.status == "success") {
                                newComment = ""
                                loadComments() // Reload setelah kirim
                            } else {
                                Toast.makeText(context, "Failed to send", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}