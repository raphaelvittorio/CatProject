package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
    navController: NavController,
    postId: Int
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load Data
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
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Comments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    ) { paddingValues ->
        // Menggunakan Box agar input bar menempel di bawah keyboard secara seamless
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // 1. DAFTAR KOMENTAR
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp), // Beri ruang untuk input bar
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comments) { item ->
                    CommentRow(item)
                }
            }

            // 2. INPUT BAR SEAMLESS (Responsive terhadap Keyboard)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding() // Otomatis menyesuaikan tinggi keyboard
                    .background(Color.White)
            ) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Pengguna
                    Image(
                        painter = rememberAsyncImagePainter(
                            "http://10.0.2.2/catpaw_api/uploads/${UserSession.currentUser?.profile_picture_url}"
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    // TextField tanpa border agar seamless
                    TextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("Add a comment...", fontSize = 14.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFFF9800)
                        ),
                        maxLines = 5
                    )

                    // Tombol Post (Style Instagram 2025)
                    TextButton(
                        onClick = {
                            if (newComment.isNotBlank()) {
                                scope.launch {
                                    val req = CommentRequest(UserSession.currentUser?.id ?: 0, postId, newComment)
                                    val res = RetrofitClient.instance.addComment(req)
                                    if (res.status == "success") {
                                        newComment = ""
                                        loadComments()
                                    } else {
                                        Toast.makeText(context, "Failed to send", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = newComment.isNotBlank()
                    ) {
                        Text(
                            "Post",
                            color = if (newComment.isNotBlank()) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentRow(item: Comment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val pp = if (!item.profile_picture_url.isNullOrEmpty())
            "http://10.0.2.2/catpaw_api/uploads/${item.profile_picture_url}"
        else "https://via.placeholder.com/150"

        Image(
            painter = rememberAsyncImagePainter(pp),
            contentDescription = null,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                buildString {
                    append(item.username)
                    append(" ")
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = item.comment,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.Black
            )

            // Sub-info (Waktu / Reply)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text("Just now", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.width(16.dp))
                Text("Reply", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    }
}