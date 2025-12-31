package com.example.catproject

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PhotoCamera
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
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val user = UserSession.currentUser

    var username by remember { mutableStateOf(user?.username ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launcher Galeri
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Tombol Save
                    IconButton(
                        onClick = {
                            if (!isLoading && user != null) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        // 1. Siapkan Data Text
                                        val uidBody = user.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val userBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())

                                        // 2. Siapkan Gambar (Jika ada)
                                        var imgBody: MultipartBody.Part? = null
                                        if (imageUri != null) {
                                            val file = File(context.cacheDir, "profile_temp.jpg")
                                            context.contentResolver.openInputStream(imageUri!!)?.copyTo(FileOutputStream(file))
                                            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                            imgBody = MultipartBody.Part.createFormData("image", file.name, reqFile)
                                        }

                                        // 3. Kirim ke API
                                        val res = RetrofitClient.instance.updateProfile(uidBody, userBody, bioBody, imgBody)

                                        if (res.status == "success") {
                                            UserSession.currentUser = res.user // Update Session
                                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFFF9800))
                        } else {
                            Icon(Icons.Rounded.Check, contentDescription = "Save", tint = Color(0xFFFF9800))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- BAGIAN FOTO PROFIL ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clickable { launcher.launch("image/*") }
            ) {
                // Gambar Profil
                val painter = if (imageUri != null) {
                    rememberAsyncImagePainter(imageUri)
                } else {
                    val url = if (user?.profile_picture_url != null)
                        "https://catpaw.my.id/catpaw_api/uploads/${user.profile_picture_url}"
                    else "https://via.placeholder.com/150"
                    rememberAsyncImagePainter(url)
                }

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                // Overlay Gelap Transparan
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Ikon Kamera
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Change profile photo",
                color = Color(0xFFFF9800), // Orange Theme
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { launcher.launch("image/*") }
            )

            Spacer(Modifier.height(32.dp))

            // --- FORM INPUT ---

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                ),
                maxLines = 4
            )
        }
    }
}