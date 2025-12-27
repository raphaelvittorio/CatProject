package com.example.catproject

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.DeletePostRequest
import com.example.catproject.network.LikeRequest
import com.example.catproject.network.Post
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.SubmitReportRequest
import kotlinx.coroutines.launch

@Composable
fun PostItem(post: Post, navController: NavController, onDelete: () -> Unit) {
    val themeColor = Color(0xFFFF9800)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Session Info
    val myId = UserSession.currentUser?.id ?: 0
    val myRole = UserSession.currentUser?.role ?: "user"

    // State
    var isLiked by remember { mutableStateOf(post.is_liked) }
    var likeCount by remember { mutableStateOf(post.like_count) }

    // Menu & Report State
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val isOwner = (post.user_id == myId)
    val isAdmin = (myRole == "admin")
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val fullImageUrl = baseUrl + post.image_url

    // --- FUNGSI SHARE (Native Android Intent) ---
    fun sharePost() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CatPaw Post")
            val shareMessage = "${post.username} shared a moment on CatPaw:\n\n$fullImageUrl\n\n\"${post.caption ?: ""}\""
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via")
        context.startActivity(shareIntent)
    }

    // --- REPORT DIALOG ---
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a reason:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    val reasons = listOf("Spam", "Inappropriate Content", "Scam / Fraud", "Hate Speech")
                    reasons.forEach { reason ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (reportReason == reason),
                                onClick = { reportReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = themeColor)
                            )
                            Text(text = reason, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotEmpty()) {
                            scope.launch {
                                try {
                                    RetrofitClient.instance.submitReport(
                                        SubmitReportRequest(myId, post.id, "post", reportReason)
                                    )
                                    Toast.makeText(context, "Report submitted.", Toast.LENGTH_SHORT).show()
                                    showReportDialog = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to submit", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    enabled = reportReason.isNotEmpty()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // --- MAIN UI ---
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 1. HEADER (Instagram Style: Profile 32dp, Bold Username, Dots Menu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ppUrl = if (!post.profile_picture_url.isNullOrEmpty()) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"

            Image(
                painter = rememberAsyncImagePainter(ppUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate("visit_profile/${post.user_id}") }
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = post.username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate("visit_profile/${post.user_id}") }
            )

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.Black)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (isOwner || isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                        if (res.status == "success") onDelete()
                                    } catch (e: Exception) {}
                                }
                            }
                        )
                    }
                    if (!isOwner) {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            leadingIcon = { Icon(Icons.Outlined.Report, null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                showReportDialog = true
                            }
                        )
                    }
                }
            }
        }

        // 2. IMAGE (Square 1:1)
        Image(
            painter = rememberAsyncImagePainter(fullImageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF5F5F5))
                .clickable { navController.navigate("post_detail/${post.id}") },
            contentScale = ContentScale.Crop
        )

        // 3. ACTION BAR (Icons: Heart, Bubble, Plane ... Bookmark)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A. Left Side Actions
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // LIKE
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFFF9800) else Color.Black,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable {
                            val oldState = isLiked
                            isLiked = !isLiked
                            likeCount += if (isLiked) 1 else -1
                            scope.launch {
                                try { RetrofitClient.instance.toggleLike(LikeRequest(myId, post.id)) }
                                catch (e: Exception) { isLiked = oldState; likeCount += if (isLiked) 1 else -1 }
                            }
                        }
                )

                // COMMENT (Chat Bubble Outline)
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.navigate("post_detail/${post.id}") }
                )

                // SHARE (Paper Plane - The "Instagram Share" look)
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Share",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { sharePost() }
                )
            }

            Spacer(Modifier.weight(1f))

            // B. Right Side Actions (Bookmark - Visual only for now)
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }

        // 4. INFO SECTION (Likes, Caption, Time)
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            // Like Count
            if (likeCount > 0) {
                Text(
                    text = "$likeCount likes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(6.dp))
            }

            // Caption (Username Bold + Text)
            if (!post.caption.isNullOrEmpty()) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(post.username + " ")
                    }
                    withStyle(style = SpanStyle(color = Color.Black)) { // Caption hitam pekat
                        append(post.caption)
                    }
                }
                Text(
                    text = annotatedString,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.clickable { navController.navigate("post_detail/${post.id}") }
                )
                Spacer(Modifier.height(4.dp))
            }

            // View all comments (Optional Text)
            Text(
                text = "View all comments",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.clickable { navController.navigate("post_detail/${post.id}") }
            )

            Spacer(Modifier.height(16.dp)) // Jarak antar post
        }
    }
}