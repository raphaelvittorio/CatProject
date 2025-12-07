package com.example.catproject

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AddPostScreen(navController: NavController) {
    var caption by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(250.dp).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
            if (imageUri != null) Image(rememberAsyncImagePainter(imageUri), null, Modifier.fillMaxSize()) else Text("Tap to select image")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Caption") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (imageUri != null && UserSession.currentUser != null) {
                scope.launch {
                    val file = File(context.cacheDir, "temp.jpg")
                    context.contentResolver.openInputStream(imageUri!!)?.copyTo(FileOutputStream(file))
                    val body = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                    val uid = UserSession.currentUser!!.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val cap = caption.toRequestBody("text/plain".toMediaTypeOrNull())

                    val res = RetrofitClient.instance.uploadPost(uid, cap, body)
                    if (res.status == "success") {
                        Toast.makeText(context, "Uploaded!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    }
                }
            }
        }) { Text("Upload") }
    }
}