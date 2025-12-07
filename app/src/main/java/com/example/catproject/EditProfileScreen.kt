package com.example.catproject

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    // State Form
    var username by remember { mutableStateOf(user?.username ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launcher untuk buka Galeri
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    // TOMBOL SAVE
                    IconButton(onClick = {
                        if (!isLoading && user != null) {
                            isLoading = true
                            scope.launch {
                                try {
                                    // 1. Siapkan Text Data
                                    val uidBody = user.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                    val userBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())

                                    // 2. Siapkan Image Data (Jika user memilih foto baru)
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
                                        UserSession.currentUser = res.user // Update Session Global
                                        Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack() // Kembali ke Profile
                                    } else {
                                        Toast.makeText(context, "Gagal: ${res.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }) {
                        if(isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Check, null, tint = Color.Blue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier.padding(p).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO PROFIL
            Box(contentAlignment = Alignment.Center) {
                // Tentukan gambar mana yang ditampilkan (Galeri baru atau URL lama)
                val painter = if (imageUri != null) {
                    rememberAsyncImagePainter(imageUri)
                } else {
                    val url = if (user?.profile_picture_url != null) "http://10.0.2.2/catpaw_api/uploads/${user.profile_picture_url}" else "https://via.placeholder.com/150"
                    rememberAsyncImagePainter(url)
                }

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(CircleShape).clickable { launcher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                "Change profile photo",
                color = Color.Blue,
                modifier = Modifier.padding(top = 8.dp).clickable { launcher.launch("image/*") }
            )

            Spacer(Modifier.height(24.dp))

            // FORM INPUT
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}