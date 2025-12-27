package com.example.catproject

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, postId: Int) {
    val themeColor = Color(0xFFFF9800)
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }

    // State untuk Menu & Dialog
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val myId = UserSession.currentUser?.id ?: 0
    val myRole = UserSession.currentUser?.role ?: "user"

    fun loadData() {
        scope.launch {
            try {
                val postRes = RetrofitClient.instance.getPostDetail(postId, myId)
                post = postRes
                comments = RetrofitClient.instance.getComments(postId)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(postId) { loadData() }

    // --- SHARE FUNCTION ---
    fun sharePost(imageUrl: String, username: String, caption: String?) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this post on CatPaw!")
            val shareMessage = "$username posted: ${caption ?: "a cute cat!"}\n\nView here: $imageUrl"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share post via")
        context.startActivity(shareIntent)
    }

    // --- REPORT DIALOG ---
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please select a reason:", fontSize = 14.sp, color = Color.Gray)
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
                            Text(text = reason, modifier = Modifier.padding(start = 8.dp))
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
                                        SubmitReportRequest(myId, postId, "post", reportReason)
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
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (post != null) {
                        val isOwner = (post!!.user_id == myId)
                        val isAdmin = (myRole == "admin")

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                if (isOwner || isAdmin) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Post", color = Color.Red) },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color.Red) },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                try {
                                                    val res = RetrofitClient.instance.deletePost(DeletePostRequest(postId, myId))
                                                    if (res.status == "success") {
                                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    }
                                                } catch (e: Exception) {}
                                            }
                                        }
                                    )
                                }
                                if (!isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Report Post") },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // --- MODERN INPUT BAR ---
            Surface(
                shadowElevation = 10.dp, // Shadow halus di atas input bar
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding() // Hindari tabrakan dengan gesture bar HP
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val myPic = if (!UserSession.currentUser?.profile_picture_url.isNullOrEmpty())
                        "http://10.0.2.2/catpaw_api/uploads/${UserSession.currentUser?.profile_picture_url}"
                    else "https://via.placeholder.com/150"

                    Image(
                        painter = rememberAsyncImagePainter(myPic),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    // Input Text Modern (Tanpa Outlined Border kasar)
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp), // Tinggi pas untuk touch target
                        shape = RoundedCornerShape(25.dp), // Bentuk Pil
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5), // Abu-abu sangat muda
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (commentText.isNotEmpty()) {
                                scope.launch {
                                    try {
                                        RetrofitClient.instance.addComment(CommentRequest(myId, postId, commentText))
                                        commentText = ""
                                        loadData()
                                    } catch (e: Exception) {}
                                }
                            }
                        })
                    )

                    Spacer(Modifier.width(8.dp))

                    // Tombol Kirim (Hanya berwarna jika ada teks)
                    val isTyping = commentText.isNotEmpty()
                    IconButton(
                        onClick = {
                            if (isTyping) {
                                scope.launch {
                                    try {
                                        RetrofitClient.instance.addComment(CommentRequest(myId, postId, commentText))
                                        commentText = ""
                                        loadData()
                                    } catch (e: Exception) {}
                                }
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            null,
                            tint = if (isTyping) themeColor else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color.White
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else if (post != null) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp), // Space agar tidak tertutup input bar
                modifier = Modifier.padding(p).fillMaxSize()
            ) {
                // --- SECTION 1: POST CONTENT ---
                item {
                    Column {
                        // User Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .clickable { navController.navigate("visit_profile/${post!!.user_id}") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val ppUrl = if (!post!!.profile_picture_url.isNullOrEmpty())
                                "http://10.0.2.2/catpaw_api/uploads/${post!!.profile_picture_url}"
                            else "https://via.placeholder.com/150"

                            Image(
                                painter = rememberAsyncImagePainter(ppUrl),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(post!!.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                // Lokasi bisa ditambah disini jika ada
                            }
                        }

                        // Full Width Image
                        Image(
                            painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${post!!.image_url}"),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // Square Image
                                .background(Color(0xFFF0F0F0)),
                            contentScale = ContentScale.Crop
                        )

                        // Action Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var isLiked by remember { mutableStateOf(post!!.is_liked) }
                            var likeCount by remember { mutableStateOf(post!!.like_count) }

                            // Like
                            Icon(
                                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) themeColor else Color.Black,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable {
                                        val oldState = isLiked
                                        isLiked = !isLiked
                                        likeCount += if (isLiked) 1 else -1
                                        scope.launch {
                                            try { RetrofitClient.instance.toggleLike(LikeRequest(myId, postId)) }
                                            catch (e: Exception) { isLiked = oldState; likeCount += if (isLiked) 1 else -1 }
                                        }
                                    }
                            )

                            Spacer(Modifier.width(20.dp))

                            // Comment (Visual Only)
                            Icon(
                                imageVector = Icons.Outlined.ModeComment,
                                contentDescription = "Comment",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )

                            Spacer(Modifier.width(20.dp))

                            // Share
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clickable {
                                        sharePost(
                                            "http://10.0.2.2/catpaw_api/uploads/${post!!.image_url}",
                                            post!!.username,
                                            post!!.caption
                                        )
                                    }
                            )
                        }

                        // Caption & Likes
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("${post!!.like_count} likes", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            if (!post!!.caption.isNullOrEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                val annotatedString = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(post!!.username + " ")
                                    }
                                    append(post!!.caption)
                                }
                                Text(text = annotatedString, fontSize = 14.sp, lineHeight = 20.sp)
                            }

                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = post!!.created_at, // Format Time Ago di Backend disarankan
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp),
                            color = Color(0xFFF0F0F0),
                            thickness = 1.dp
                        )
                    }
                }

                // --- SECTION 2: COMMENTS LIST ---
                if (comments.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.ModeComment, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No comments yet.", color = Color.Gray, fontSize = 14.sp)
                                Text("Start the conversation.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Top // Avatar sejajar teks baris pertama
                        ) {
                            val cPp = if (!comment.profile_picture_url.isNullOrEmpty())
                                "http://10.0.2.2/catpaw_api/uploads/${comment.profile_picture_url}"
                            else "https://via.placeholder.com/150"

                            Image(
                                painter = rememberAsyncImagePainter(cPp),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                val annotatedString = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                                        append(comment.username + " ")
                                    }
                                    append(comment.comment)
                                }
                                Text(text = annotatedString, fontSize = 13.sp, lineHeight = 18.sp)
                                // Bisa tambah reply button atau time di sini
                            }
                        }
                    }
                }
            }
        }
    }
}