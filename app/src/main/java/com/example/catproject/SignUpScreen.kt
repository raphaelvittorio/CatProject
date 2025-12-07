package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.SignupRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF9800))
            Text("Join the community now", color = Color.Gray)

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // PERBAIKAN: Gunakan OutlinedTextFieldDefaults.colors
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                )
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // PERBAIKAN: Gunakan OutlinedTextFieldDefaults.colors
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                )
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // PERBAIKAN: Gunakan OutlinedTextFieldDefaults.colors
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                )
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val res = RetrofitClient.instance.signup(SignupRequest(username, password, email))
                            if (res.status == "success") {
                                Toast.makeText(context, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error connection", Toast.LENGTH_SHORT).show()
                        } finally { isLoading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Row {
                Text("Already have an account? ", color = Color.Gray)
                Text("Log In", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.popBackStack() })
            }
        }
    }
}