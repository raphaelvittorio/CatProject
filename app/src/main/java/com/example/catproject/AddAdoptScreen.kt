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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
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
fun AddAdoptScreen(navController: NavController) {
    var catName by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List for Adoption") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEFEF))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(rememberAsyncImagePainter(imageUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(40.dp), tint = Color.Gray)
                        Text("Add Cat Photo", color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Form
            OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Cat Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (Age, Breed, etc)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact (WA/Phone)") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (imageUri != null && catName.isNotEmpty() && contact.isNotEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val file = File(context.cacheDir, "adopt_temp.jpg")
                                context.contentResolver.openInputStream(imageUri!!)?.copyTo(FileOutputStream(file))
                                val imgPart = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))

                                val uid = UserSession.currentUser!!.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val n = catName.toRequestBody("text/plain".toMediaTypeOrNull())
                                val d = desc.toRequestBody("text/plain".toMediaTypeOrNull())
                                val c = contact.toRequestBody("text/plain".toMediaTypeOrNull())

                                val res = RetrofitClient.instance.uploadAdoption(uid, n, d, c, imgPart)
                                if (res.status == "success") {
                                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) { Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show() }
                            finally { isLoading = false }
                        }
                    } else { Toast.makeText(context, "Please fill all fields & photo", Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if(isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("List for Adoption")
            }
        }
    }
}