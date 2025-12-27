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
import androidx.compose.material.icons.filled.Bookmark
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
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@Composable
fun PostItem(post: Post, navController: NavController, onDelete: () -> Unit) {
    val themeColor = Color(0xFFFF9800)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val myId = UserSession.currentUser?.id ?: 0
    val myRole = UserSession.currentUser?.role ?: "user"

    // State
    var isLiked by remember { mutableStateOf(post.is_liked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    var isSaved by remember { mutableStateOf(post.is_saved) }

    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val isOwner = (post.user_id == myId)
    val isAdmin = (myRole == "admin")
    val baseUrl = "http://10.0.2.2/catpaw_api/uploads/"
    val fullImageUrl = baseUrl + post.image_url

    fun sharePost() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "CatPaw Post")
            val shareMessage = "${post.username} shared: $fullImageUrl\n\n${post.caption ?: ""}"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via")
        context.startActivity(shareIntent)
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post") },
            text = {
                Column {
                    val reasons = listOf("Spam", "Inappropriate", "Scam", "Hate Speech")
                    reasons.forEach { reason ->
                        Row(Modifier.fillMaxWidth().clickable { reportReason = reason }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (reportReason == reason), onClick = { reportReason = reason })
                            Text(reason, Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            RetrofitClient.instance.submitReport(SubmitReportRequest(myId, post.id, "post", reportReason))
                            Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
                            showReportDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to submit report", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("Cancel") } }
        )
    }

    Column(Modifier.fillMaxWidth().background(Color.White)) {
        // Header
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val ppUrl = if (!post.profile_picture_url.isNullOrEmpty()) baseUrl + post.profile_picture_url else "https://via.placeholder.com/150"
            Image(rememberAsyncImagePainter(ppUrl), null, Modifier.size(32.dp).clip(CircleShape).clickable { navController.navigate("visit_profile/${post.user_id}") }, contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp))
            Text(post.username, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Rounded.MoreVert, null) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    // --- DELETE LOGIC IMPLEMENTED HERE ---
                    if (isOwner || isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.instance.deletePost(DeletePostRequest(post.id, myId))
                                        if (res.status == "success") {
                                            Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                            onDelete() // Refresh list UI
                                        } else {
                                            Toast.makeText(context, "Failed to delete: ${res.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                    if (!isOwner) {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            leadingIcon = { Icon(Icons.Outlined.Report, null) },
                            onClick = {
                                showMenu = false
                                showReportDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Image
        Image(rememberAsyncImagePainter(fullImageUrl), null, Modifier.fillMaxWidth().aspectRatio(1f).clickable { navController.navigate("post_detail/${post.id}") }, contentScale = ContentScale.Crop)

        // Actions
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder, "Like",
                    tint = if (isLiked) themeColor else Color.Black,
                    modifier = Modifier.size(26.dp).clickable {
                        isLiked = !isLiked; likeCount += if (isLiked) 1 else -1
                        scope.launch { try { RetrofitClient.instance.toggleLike(LikeRequest(myId, post.id)) } catch (e: Exception) {} }
                    }
                )
                Icon(Icons.Outlined.ChatBubbleOutline, "Comment", Modifier.size(24.dp).clickable { navController.navigate("post_detail/${post.id}") })
                Icon(Icons.AutoMirrored.Outlined.Send, "Share", Modifier.size(24.dp).clickable { sharePost() })
            }
            Spacer(Modifier.weight(1f))

            // TOMBOL SAVE
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = if (isSaved) themeColor else Color.Black,
                modifier = Modifier.size(26.dp).clickable {
                    val oldState = isSaved
                    isSaved = !isSaved
                    scope.launch {
                        try {
                            val res = RetrofitClient.instance.toggleSave(SaveRequest(myId, post.id))
                            if (res.status == "success") {
                                Toast.makeText(context, if (res.action == "saved") "Saved" else "Unsaved", Toast.LENGTH_SHORT).show()
                            } else { isSaved = oldState }
                        } catch (e: Exception) { isSaved = oldState }
                    }
                }
            )
        }

        // Footer
        Column(Modifier.padding(horizontal = 12.dp)) {
            if (likeCount > 0) Text("$likeCount likes", fontWeight = FontWeight.Bold)
            if (!post.caption.isNullOrEmpty()) {
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(post.username + " ") }
                    append(post.caption)
                })
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}