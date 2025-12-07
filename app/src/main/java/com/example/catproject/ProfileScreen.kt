package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.catproject.network.FollowRequest
import com.example.catproject.network.ProfileResponse
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    rootNavController: NavController,
    targetUserId: Int? = null // Null = Tab Utama, Tidak Null = Hasil Navigasi
) {
    // 1. STATE VARIABLES
    var data by remember { mutableStateOf<ProfileResponse?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Tentukan ID User
    val myId = UserSession.currentUser?.id ?: 0
    val profileId = targetUserId ?: myId // ID profil yang akan ditampilkan
    val isMe = (profileId == myId) // Apakah ini profil saya?

    // LOGIC TOMBOL BACK:
    // Tampilkan Back jika targetUserId TIDAK NULL (artinya kita masuk lewat klik link/navigasi)
    // Sembunyikan Back jika targetUserId NULL (artinya kita di Tab Menu Utama)
    val showBackButton = (targetUserId != null)

    // State Lokal UI
    var isFollowing by remember { mutableStateOf(false) }
    var followerCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var postCount by remember { mutableStateOf(0) }

    // 2. LOAD DATA
    fun loadProfile() {
        scope.launch {
            try {
                isLoading = true
                val res = RetrofitClient.instance.getProfile(
                    userId = profileId,
                    viewerId = myId
                )

                data = res
                isFollowing = res.is_following
                followerCount = res.stats.followers
                followingCount = res.stats.following
                postCount = res.stats.posts

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(profileId) { loadProfile() }

    // 3. UI LAYOUT
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = data?.user?.username ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                // --- UPDATE PENTING DI SINI ---
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Menu Logout tetap hanya muncul di profil sendiri (isMe)
                    if (isMe) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Menu, null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    UserSession.currentUser = null
                                    rootNavController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        if (data != null) {
            Column(
                modifier = Modifier
                    .padding(p)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // --- HEADER ---
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pp = if (data!!.user.profile_picture_url != null)
                        "http://10.0.2.2/catpaw_api/uploads/${data!!.user.profile_picture_url}"
                    else "https://via.placeholder.com/150"

                    AsyncImage(
                        model = pp,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(postCount.toString(), "Posts")
                        StatItem(followerCount.toString(), "Followers")
                        StatItem(followingCount.toString(), "Following")
                    }
                }

                // --- BIO ---
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = data!!.user.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!data!!.user.bio.isNullOrEmpty()) {
                        Text(
                            text = data!!.user.bio!!,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // --- BUTTONS ---
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    if (isMe) {
                        // Tampilan Profil Sendiri (Edit)
                        Button(
                            onClick = { navController.navigate("edit_profile") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit profile", color = Color.Black)
                        }
                    } else {
                        // Tampilan Profil Orang Lain (Follow)
                        val btnColor = if (isFollowing) Color(0xFFEFEFEF) else MaterialTheme.colorScheme.primary
                        val txtColor = if (isFollowing) Color.Black else Color.White
                        val txt = if (isFollowing) "Unfollow" else "Follow"

                        Button(
                            onClick = {
                                isFollowing = !isFollowing
                                followerCount += if (isFollowing) 1 else -1
                                scope.launch {
                                    try { RetrofitClient.instance.toggleFollow(FollowRequest(myId, profileId)) }
                                    catch (e: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(txt, color = txtColor, fontWeight = FontWeight.Bold)
                        }
                        // TOMBOL MESSAGE
                        Button(
                            onClick = {
                                // Navigasi ke Chat Detail dengan Orang Ini
                                navController.navigate("chat_detail/${profileId}/${data!!.user.username}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Message", color = Color.Black)
                        }
                    }
                }

                // --- GRID ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Icon(Icons.Default.GridOn, null, modifier = Modifier.size(28.dp))
                    Icon(Icons.Outlined.AccountBox, null, modifier = Modifier.size(28.dp), tint = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))

                if (data!!.posts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No Posts Yet", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(data!!.posts) { post ->
                            AsyncImage(
                                model = "http://10.0.2.2/catpaw_api/uploads/${post.image_url}",
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { navController.navigate("post_detail/${post.id}") },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 13.sp)
    }
}