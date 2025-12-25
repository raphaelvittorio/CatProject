package com.example.catproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
// Import Kelas TFLite yang digenerate otomatis
import com.example.catproject.ml.CatBreedMetadata
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdoptScreen(navController: NavController) {
    // --- STATE VARIABLES ---
    var catName by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var classificationResult by remember { mutableStateOf<String?>(null) }
    var confidenceScore by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- CEK SESSION SAAT LAYAR DIBUKA ---
    LaunchedEffect(Unit) {
        val myId = UserSession.currentUser?.id ?: 0
        if (myId == 0) {
            Toast.makeText(context, "Session Expired. Silakan Login Ulang!", Toast.LENGTH_LONG).show()
            // Opsional: Lempar user kembali ke halaman login jika perlu
            // navController.navigate("login")
        }
    }

    // --- FUNGSI ANALISA TFLITE (AI) ---
    fun analyzeImage(uri: Uri) {
        classificationResult = "Analyzing..."

        scope.launch(Dispatchers.Default) {
            try {
                // 1. Convert URI ke Bitmap
                val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                // 2. WAJIB: Convert ke ARGB_8888 (Format TFLite)
                val tfBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                // 3. Load Model (Otomatis dari file .tflite)
                val model = CatBreedMetadata.newInstance(context)

                // 4. Proses Gambar
                val image = TensorImage.fromBitmap(tfBitmap)
                val outputs = model.process(image)
                val probability = outputs.probabilityAsCategoryList

                // 5. Cari skor tertinggi
                val bestMatch = probability.maxByOrNull { it.score }

                // 6. Update UI
                withContext(Dispatchers.Main) {
                    if (bestMatch != null) {
                        val breed = bestMatch.label
                        val score = bestMatch.score * 100

                        classificationResult = "$breed (${score.toInt()}%)"
                        confidenceScore = score

                        // Auto-fill deskripsi jika kosong
                        if (desc.isEmpty()) {
                            desc = "Ras: $breed"
                        }
                    } else {
                        classificationResult = "Unknown Breed"
                    }
                    model.close() // Tutup model untuk hemat memori
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    classificationResult = "AI Error: ${e.message}"
                }
            }
        }
    }

    // --- IMAGE PICKER ---
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            analyzeImage(uri) // Analisa otomatis saat gambar dipilih
        }
    }

    // --- UI STRUCTURE ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("List for Adoption", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, null)
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
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
        ) {

            // 1. HERO IMAGE UPLOADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFEEEEEE))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay Hasil AI
                    if (classificationResult != null) {
                        Surface(
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 24.dp)
                        ) {
                            Text(
                                text = classificationResult ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to upload cat photo", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 2. FORM SECTION
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Pet Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Info AI di atas form
                if (classificationResult != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFFFF9800))
                        Spacer(Modifier.width(8.dp))
                        Text(text = "AI Detected: $classificationResult", color = Color.DarkGray, fontSize = 14.sp)
                    }
                }

                // Input Fields
                ModernAdoptTextField(value = catName, onValueChange = { catName = it }, label = "Cat Name", icon = Icons.Rounded.Pets)
                Spacer(Modifier.height(12.dp))
                ModernAdoptTextField(value = contact, onValueChange = { contact = it }, label = "WhatsApp / Contact", icon = Icons.Rounded.Phone)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Rounded.Description, null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFFF9800),
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                Spacer(Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (imageUri != null && catName.isNotEmpty() && contact.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    // A. Ambil User ID dari Session (PERBAIKAN: Gunakan UserSession object)
                                    val myId = UserSession.currentUser?.id ?: 0

                                    // Cek apakah user sudah login
                                    if (myId == 0) {
                                        Toast.makeText(context, "Session Expired. Harap Login Ulang!", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                        return@launch
                                    }

                                    // B. Siapkan File Gambar (Buat Temporary File)
                                    val file = File(context.cacheDir, "adopt_temp_${System.currentTimeMillis()}.jpg")
                                    context.contentResolver.openInputStream(imageUri!!)?.use { input ->
                                        FileOutputStream(file).use { output ->
                                            input.copyTo(output)
                                        }
                                    }

                                    // C. Siapkan Request Body untuk Retrofit
                                    val imgPart = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                                    val uid = myId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                    val n = catName.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val d = desc.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val c = contact.toRequestBody("text/plain".toMediaTypeOrNull())

                                    // D. Kirim ke Server
                                    val res = RetrofitClient.instance.uploadAdoption(uid, n, d, c, imgPart)

                                    if (res.status == "success") {
                                        Toast.makeText(context, "Berhasil Diposting!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack() // Kembali ke layar sebelumnya
                                    } else {
                                        Toast.makeText(context, "Gagal: ${res.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Mohon lengkapi Nama, Kontak & Foto", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Publish Listing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER COMPOSABLE (Agar desain input rapi) ---
@Composable
fun ModernAdoptTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFFF9800),
            unfocusedIndicatorColor = Color.LightGray
        ),
        singleLine = true
    )
}