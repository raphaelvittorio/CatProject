package com.example.catproject

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    // STATE
    var caption by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // STATE MODE: true = Upload Story, false = Upload Post
    var isStoryMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val myId = UserSession.currentUser?.id ?: 0

    // IMAGE PICKER
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUri = uri
    }

    // FUNGSI UPLOAD
    fun uploadData() {
        if (imageUri == null) {
            Toast.makeText(context, "Pilih foto dulu!", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                // 1. Siapkan File Gambar
                val file = uriToFile(context, imageUri!!)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val userIdPart = myId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // 2. Cek Mode (Story atau Post)
                if (isStoryMode) {
                    // --- UPLOAD STORY ---
                    val res = RetrofitClient.instance.uploadStory(userIdPart, imagePart)
                    if (res.status == "success") {
                        Toast.makeText(context, "Story berhasil diupload!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Gagal: ${res.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // --- UPLOAD POST ---
                    val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())
                    val res = RetrofitClient.instance.uploadPost(userIdPart, captionPart, imagePart)
                    if (res.status == "success") {
                        Toast.makeText(context, "Postingan berhasil!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Gagal: ${res.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isStoryMode) "New Story" else "New Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Black)
                    }
                },
                actions = {
                    // Tombol Share / Post (Text Button Modern)
                    TextButton(
                        onClick = { uploadData() },
                        enabled = !isLoading && imageUri != null
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFFF9800))
                        } else {
                            Text(
                                "Share",
                                color = if (imageUri != null) Color(0xFFFF9800) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // TAB SELECTOR (POST / STORY)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Tab POST
                Text(
                    "POST",
                    modifier = Modifier
                        .clickable { isStoryMode = false }
                        .padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = if (!isStoryMode) Color.Black else Color.Gray,
                    fontSize = 16.sp
                )

                // Divider Kecil
                Text("|", color = Color.LightGray)

                // Tab STORY
                Text(
                    "STORY",
                    modifier = Modifier
                        .clickable { isStoryMode = true }
                        .padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = if (isStoryMode) Color.Black else Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {

            // AREA GAMBAR & CAPTION (Hanya muncul caption jika Post Mode)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // 1. KOTAK GAMBAR (KIRI)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f) // Kotak Persegi
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay ganti foto
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.1f))
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Select Photo", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                // 2. INPUT CAPTION (KANAN) - HANYA MUNCUL DI MODE POST
                if (!isStoryMode) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1.5f)) {
                        BasicTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (caption.isEmpty()) {
                                    Text("Write a caption...", color = Color.Gray, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PESAN HINT
            if (isStoryMode) {
                Text(
                    "Stories disappear after 24 hours.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// --- HELPER: KONVERSI URI KE FILE ---
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "upload_temp.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}