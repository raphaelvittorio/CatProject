package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// IMPORT IKON MODERN
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert // Titik tiga modern pengganti Menu
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
    targetUserId: Int? = null
) {
    // ... (BAGIAN STATE & LOAD DATA SAMA SEPERTI SEBELUMNYA) ...
    var data by remember { mutableStateOf<ProfileResponse?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val myId = UserSession.currentUser?.id ?: 0
    val profileId = targetUserId ?: myId
    val isMe = (profileId == myId)
    var isFollowing by remember { mutableStateOf(false) }
    var followerCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var postCount by remember { mutableStateOf(0) }
    fun loadProfile() {
        scope.launch {
            try {
                isLoading = true
                val res = RetrofitClient.instance.getProfile(userId = profileId, viewerId = myId)
                data = res
                isFollowing = res.is_following
                followerCount = res.stats.followers
                followingCount = res.stats.following
                postCount = res.stats.posts
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoading = false }
        }
    }
    LaunchedEffect(profileId) { loadProfile() }
    // ... (SAMPAI SINI SAMA) ...

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(data?.user?.username ?: "Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    if (!isMe) {
                        // UPDATE: Ikon Back Modern
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isMe) {
                        // UPDATE: Ikon Menu Titik Tiga Modern
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, null)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    UserSession.currentUser = null
                                    rootNavController.navigate("login") { popUpTo(0) { inclusive = true } }
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
            Column(modifier = Modifier.padding(p).fillMaxSize().background(Color.White)) {
                // ... (BAGIAN HEADER, STATS, BIO, BUTTONS SAMA SEPERTI SEBELUMNYA) ...
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pp = if (data!!.user.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${data!!.user.profile_picture_url}" else "https://via.placeholder.com/150"
                    AsyncImage(model = pp, contentDescription = null, modifier = Modifier.size(86.dp).clip(CircleShape).border(1.dp, Color.LightGray, CircleShape), contentScale = ContentScale.Crop)
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        ModernStatItem(postCount.toString(), "Posts")
                        ModernStatItem(followerCount.toString(), "Followers")
                        ModernStatItem(followingCount.toString(), "Following")
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(text = data!!.user.username, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (!data!!.user.bio.isNullOrEmpty()) { Spacer(Modifier.height(4.dp)); Text(text = data!!.user.bio!!, fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray) }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (isMe) {
                        ModernProfileButton("Edit profile", Modifier.weight(1f)) { navController.navigate("edit_profile") }
                        Spacer(Modifier.width(8.dp))
                        ModernProfileButton("Share profile", Modifier.weight(1f)) { }
                    } else {
                        val isFollowed = isFollowing
                        val btnColor = if (isFollowed) Color(0xFFEFEFEF) else Color(0xFFFF9800)
                        val txtColor = if (isFollowed) Color.Black else Color.White
                        val txt = if (isFollowed) "Unfollow" else "Follow"
                        Button(onClick = { isFollowing = !isFollowing; followerCount += if (isFollowing) 1 else -1; scope.launch { try { RetrofitClient.instance.toggleFollow(FollowRequest(myId, profileId)) } catch (e: Exception) {} } }, colors = ButtonDefaults.buttonColors(containerColor = btnColor), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(36.dp), contentPadding = PaddingValues(0.dp)) {
                            Text(txt, color = txtColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        ModernProfileButton("Message", Modifier.weight(1f)) { navController.navigate("chat_detail/${profileId}/${data!!.user.username}") }
                    }
                }
                Spacer(Modifier.height(16.dp))
                // ... (SAMPAI SINI SAMA) ...

                // --- TABS (UPDATE IKON MODERN) ---
                Row(modifier = Modifier.fillMaxWidth()) {
                    TabIconItem(
                        isActive = selectedTabIndex == 0,
                        // Ikon Grid Modern (Rounded vs Outlined)
                        iconActive = Icons.Rounded.GridOn,
                        iconInactive = Icons.Outlined.GridOnOutlined,
                        onClick = { selectedTabIndex = 0 }
                    )
                    TabIconItem(
                        isActive = selectedTabIndex == 1,
                        // Ikon Pets Modern (Rounded vs Outlined)
                        iconActive = Icons.Rounded.Pets,
                        iconInactive = Icons.Outlined.PetsOutlined,
                        onClick = { selectedTabIndex = 1 }
                    )
                }

                // --- GRID CONTENT (SAMA SEPERTI SEBELUMNYA) ---
                if (selectedTabIndex == 0) {
                    if (data!!.posts.isEmpty()) { EmptyStateView("No Posts Yet", Icons.Outlined.GridOnOutlined) } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            items(data!!.posts) { post -> AsyncImage(model = "http://10.0.2.2/catpaw_api/uploads/${post.image_url}", contentDescription = null, modifier = Modifier.aspectRatio(1f).clickable { navController.navigate("post_detail/${post.id}") }, contentScale = ContentScale.Crop) }
                        }
                    }
                } else {
                    if (data!!.adopted_cats.isEmpty()) { EmptyStateView("No Adopted Cats", Icons.Outlined.PetsOutlined) } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            items(data!!.adopted_cats) { cat -> Box { AsyncImage(model = "http://10.0.2.2/catpaw_api/uploads/${cat.image_url}", contentDescription = null, modifier = Modifier.aspectRatio(1f), contentScale = ContentScale.Crop); Surface(color = Color.Black.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) { Text(text = cat.cat_name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } } }
                        }
                    }
                }
            }
        } else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF9800)) } }
    }
}

// --- SUB COMPONENTS (SAMA SEPERTI SEBELUMNYA) ---
@Composable fun ModernStatItem(count: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = count, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text(text = label, fontSize = 13.sp, color = Color.Gray) } }
@Composable fun RowScope.ModernProfileButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) { Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)), shape = RoundedCornerShape(8.dp), modifier = modifier.height(36.dp), contentPadding = PaddingValues(0.dp)) { Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) } }
@Composable fun RowScope.TabIconItem(isActive: Boolean, iconActive: androidx.compose.ui.graphics.vector.ImageVector, iconInactive: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { Box(modifier = Modifier.weight(1f).height(48.dp).clickable { onClick() }.border(width = if (isActive) 1.dp else 0.dp, color = if (isActive) Color(0xFFFF9800) else Color.Transparent, shape = androidx.compose.ui.graphics.RectangleShape), contentAlignment = Alignment.Center) { Icon(imageVector = if (isActive) iconActive else iconInactive, contentDescription = null, tint = if (isActive) Color(0xFFFF9800) else Color.Gray, modifier = Modifier.size(24.dp)) } }
@Composable fun EmptyStateView(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Column(modifier = Modifier.fillMaxSize().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Box(modifier = Modifier.size(80.dp).border(2.dp, Color.Black, CircleShape).padding(16.dp), contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(40.dp), tint = Color.Black) }; Spacer(Modifier.height(16.dp)); Text(message, fontWeight = FontWeight.Bold, fontSize = 20.sp) } }