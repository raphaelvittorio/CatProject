package com.example.catproject

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowBack
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
fun AddPostScreen(navController: NavController) {
    var caption by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ambil data user session untuk foto profil mini
    val currentUser = UserSession.currentUser

    // Launcher Galeri
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Post", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    // Tombol Share Modern (Teks Biru)
                    TextButton(
                        onClick = {
                            if (imageUri != null && currentUser != null) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        // 1. Proses File Gambar
                                        val file = File(context.cacheDir, "temp_upload.jpg")
                                        context.contentResolver.openInputStream(imageUri!!)?.copyTo(FileOutputStream(file))

                                        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                        val body = MultipartBody.Part.createFormData("image", file.name, reqFile)
                                        val uid = currentUser.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val cap = caption.toRequestBody("text/plain".toMediaTypeOrNull())

                                        // 2. Upload ke API
                                        val response = RetrofitClient.instance.uploadPost(uid, cap, body)

                                        if (response.status == "success") {
                                            Toast.makeText(context, "Shared successfully!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "Failed: ${response.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            "Share",
                            color = if (isLoading) Color.Gray else Color(0xFFFF9800), // Biru Instagram
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Linear Progress Bar (Muncul saat loading)
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3797EF)
                )
            }

            // --- AREA CAPTION & USER INFO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Mini Avatar User
                val pp = if(currentUser?.profile_picture_url != null)
                    "http://10.0.2.2/catpaw_api/uploads/${currentUser.profile_picture_url}"
                else "https://via.placeholder.com/150"

                Image(
                    painter = rememberAsyncImagePainter(pp),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Input Caption (Minimalis tanpa border)
                TextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Write a caption...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp), // Tinggi area ketik
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
            }

            Divider(color = Color(0xFFEFEFEF), thickness = 1.dp)

            // --- AREA GAMBAR (HERO IMAGE) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Kotak 1:1
                    .background(Color(0xFFF8F8F8))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Tombol Ganti Kecil di pojok
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "Tap to change",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Photo", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}