package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.ApplyAdoptionRequest
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyAdoptionScreen(
    navController: NavController,
    adoptionId: Int,
    catName: String
) {
    var message by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adoption Form", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Adopt $catName",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF9800)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Please fill out your details. Once submitted, the adoption will be processed immediately (Demo Mode).",
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Contact Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Reason for adoption") },
                        placeholder = { Text("I love cats because...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if(phone.isNotEmpty() && message.isNotEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val userId = UserSession.currentUser?.id ?: 0
                                val req = ApplyAdoptionRequest(adoptionId, userId, message, phone)

                                // Panggil API Apply
                                val res = RetrofitClient.instance.applyAdoption(req)

                                if(res.status == "success") {
                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                    navController.popBackStack() // Kembali ke List
                                } else {
                                    Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                enabled = !isLoading
            ) {
                if(isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Application", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}