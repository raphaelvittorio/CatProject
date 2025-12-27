package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// IMPORT IKON MODERN
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.outlined.GridOn as GridOnOutlined
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.outlined.Pets as PetsOutlined
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
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.FollowRequest
import com.example.catproject.network.ProfileResponse
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    rootNavController: NavController,
    targetUserId: Int? = null
) {
    // --- LOGIC SETUP ---
    val myId = UserSession.currentUser?.id ?: 0
    val profileId = targetUserId ?: myId
    val isMe = (profileId == myId)

    // State Data
    var data by remember { mutableStateOf<ProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Posts, 1 = Adoptions

    // State Statistik (Reaktif)
    var isFollowing by remember { mutableStateOf(false) }
    var followerCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var postCount by remember { mutableStateOf(0) }

    // Menu Logout
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load Profile Data
    fun loadProfile() {
        scope.launch {
            try {
                isLoading = true
                val res = RetrofitClient.instance.getProfile(userId = profileId, viewerId = myId)
                data = res
                // Set data awal ke state reaktif
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        data?.user?.username ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    if (!isMe) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isMe) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    UserSession.currentUser = null
                                    try { rootNavController.navigate("login") { popUpTo(0) } } catch (e: Exception) { navController.navigate("login") }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { p ->
        if (data != null && !isLoading) {
            val user = data!!.user
            val posts = data!!.posts
            val adoptions = data!!.adopted_cats

            // --- SEAMLESS SCROLLING (Grid Utama) ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = p,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. HEADER SECTION (Menggunakan Span 3 agar selebar layar)
                item(span = { GridItemSpan(3) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // A. Foto Profil & Statistik
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Foto Profil
                            val ppUrl = if (!user.profile_picture_url.isNullOrEmpty())
                                "http://10.0.2.2/catpaw_api/uploads/${user.profile_picture_url}"
                            else "https://via.placeholder.com/150"

                            Image(
                                painter = rememberAsyncImagePainter(ppUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.LightGray, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.width(24.dp))

                            // Statistik Row
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatItemThemed(postCount.toString(), "Posts")
                                StatItemThemed(followerCount.toString(), "Followers")
                                StatItemThemed(followingCount.toString(), "Following")
                            }
                        }

                        // B. Bio & Nama
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Text(user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (!user.bio.isNullOrEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(user.bio, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // C. Action Buttons (Edit / Follow / Admin)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isMe) {
                                // Tombol Edit (Standard)
                                ActionButtonThemed("Edit Profile", Modifier.weight(1f)) {
                                    navController.navigate("edit_profile")
                                }

                                // --- TOMBOL ADMIN PANEL (HANYA UNTUK ADMIN) ---
                                if (UserSession.currentUser?.role == "admin") {
                                    Button(
                                        onClick = { navController.navigate("admin_dashboard") },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                    ) {
                                        Text("Admin Panel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                // ----------------------------------------------
                            } else {
                                // Tombol Follow (Filled Orange Style)
                                val btnColor = if (isFollowing) Color(0xFFEFEFEF) else Color(0xFFFF9800) // Orange Theme
                                val txtColor = if (isFollowing) Color.Black else Color.White
                                val txt = if (isFollowing) "Unfollow" else "Follow"

                                Button(
                                    onClick = {
                                        isFollowing = !isFollowing
                                        followerCount += if (isFollowing) 1 else -1
                                        scope.launch { try { RetrofitClient.instance.toggleFollow(FollowRequest(myId, profileId)) } catch (e: Exception) {} }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(txt, color = txtColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                // Tombol Message (Outline Style)
                                ActionButtonThemed("Message", Modifier.weight(1f)) {
                                    navController.navigate("chat_detail/${profileId}/${user.username}")
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // D. Tab Switcher Modern
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TabItemThemed(
                                isActive = selectedTab == 0,
                                iconActive = Icons.Rounded.GridOn,
                                iconInactive = Icons.Outlined.GridOnOutlined,
                                onClick = { selectedTab = 0 }
                            )
                            TabItemThemed(
                                isActive = selectedTab == 1,
                                iconActive = Icons.Rounded.Pets,
                                iconInactive = Icons.Outlined.PetsOutlined,
                                onClick = { selectedTab = 1 }
                            )
                        }
                    }
                }

                // 2. GRID KONTEN (POSTS / ADOPTIONS)
                if (selectedTab == 0) {
                    // --- TAB POSTS ---
                    if (posts.isEmpty()) {
                        item(span = { GridItemSpan(3) }) { EmptyStateThemed("No posts yet") }
                    } else {
                        items(posts) { post ->
                            Image(
                                painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${post.image_url}"),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .border(0.5.dp, Color.White)
                                    .clickable { navController.navigate("post_detail/${post.id}") },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // --- TAB ADOPTIONS ---
                    if (adoptions.isEmpty()) {
                        item(span = { GridItemSpan(3) }) { EmptyStateThemed("No adoption listings") }
                    } else {
                        items(adoptions) { cat ->
                            Box(modifier = Modifier.aspectRatio(1f).border(0.5.dp, Color.White)) {
                                Image(
                                    painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${cat.image_url}"),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Overlay Nama Kucing (Gradient/Darken)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = cat.cat_name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Loading State (Orange Indicator)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG TEMA ---

@Composable
fun StatItemThemed(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
fun ActionButtonThemed(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) // Garis halus
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun RowScope.TabItemThemed(
    isActive: Boolean,
    iconActive: androidx.compose.ui.graphics.vector.ImageVector,
    iconInactive: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp)) {
            Icon(
                imageVector = if (isActive) iconActive else iconInactive,
                contentDescription = null,
                tint = if (isActive) Color.Black else Color.Gray, // Hitam saat aktif
                modifier = Modifier.size(24.dp)
            )
        }
        // Garis Indikator Bawah
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp) // Sedikit lebih tebal agar terlihat
                .background(if (isActive) Color.Black else Color(0xFFF5F5F5))
        )
    }
}

@Composable
fun EmptyStateThemed(msg: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.GridOnOutlined, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(msg, color = Color.Gray, fontSize = 14.sp)
    }
}