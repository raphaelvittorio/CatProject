package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.StoryItem
import kotlinx.coroutines.delay

@Composable
fun StoryViewScreen(navController: NavController, userId: Int) {
    var stories by remember { mutableStateOf<List<StoryItem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // 1. Load Data
    LaunchedEffect(userId) {
        try {
            val result = RetrofitClient.instance.getUserStories(userId)
            if (result.isNotEmpty()) {
                stories = result
                isLoading = false
            } else {
                navController.popBackStack() // Jika kosong, kembali
            }
        } catch (e: Exception) {
            navController.popBackStack()
        }
    }

    // 2. Timer Logic (60 FPS Update + Pause Feature)
    LaunchedEffect(currentIndex, stories, isLoading, isPaused) {
        if (!isLoading && stories.isNotEmpty() && !isPaused) {
            val startTime = System.currentTimeMillis() - (progress * 5000L).toLong()

            while (progress < 1f) {
                if (!isPaused) {
                    progress = (System.currentTimeMillis() - startTime).toFloat() / 5000L
                }
                delay(16) // Update halus
            }
            // Pindah Next Story
            if (currentIndex < stories.size - 1) {
                currentIndex++
                progress = 0f
            } else {
                navController.popBackStack() // Selesai, kembali ke Home
            }
        }
    }

    // 3. UI Full Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidthPx = size.width
                        if (offset.x < screenWidthPx / 2) {
                            // Tap Kiri (Prev)
                            if (currentIndex > 0) {
                                currentIndex--
                                progress = 0f
                            }
                        } else {
                            // Tap Kanan (Next)
                            if (currentIndex < stories.size - 1) {
                                currentIndex++
                                progress = 0f
                            } else {
                                navController.popBackStack()
                            }
                        }
                    }
                )
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else if (stories.isNotEmpty()) {
            val currentStory = stories[currentIndex]
            val baseUrl = "https://catpaw.my.id/catpaw_api/uploads/"

            // GAMBAR UTAMA
            Image(
                painter = rememberAsyncImagePainter(baseUrl + currentStory.image_url),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // GRADIENT SCRIM (Bayangan Hitam di Atas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // UI HEADER (Progress & User Info)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp)
            ) {
                // Progress Bar Segmented
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        val p = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    val avatarUrl = if (!currentStory.profile_picture_url.isNullOrEmpty())
                        baseUrl + currentStory.profile_picture_url
                    else "https://via.placeholder.com/150"

                    Image(
                        painter = rememberAsyncImagePainter(avatarUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = currentStory.username ?: "User",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Tombol Close
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}