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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.LoginRequest
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- LOGO ---
            Text(
                text = "CatPaw",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                letterSpacing = (-2).sp,
                color = Color(0xFFFF9800) // Orange Brand
            )

            Text(
                "Connect with cat lovers",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(48.dp))

            // --- INPUT FIELDS ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // PERBAIKAN: Gunakan OutlinedTextFieldDefaults.colors
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                // PERBAIKAN: Gunakan OutlinedTextFieldDefaults.colors
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9800),
                    focusedLabelColor = Color(0xFFFF9800),
                    cursorColor = Color(0xFFFF9800)
                ),
                singleLine = true
            )

            // Forgot Password (Dummy)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { /* TODO */ }) {
                    Text("Forgot password?", color = Color(0xFF3797EF))
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- LOGIN BUTTON ---
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.login(LoginRequest(username, password))
                                if (res.status == "success") {
                                    UserSession.currentUser = res.user
                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("main_app") { popUpTo("login") { inclusive = true } }
                                } else {
                                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error connection", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))

            // --- SIGN UP LINK ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Sign Up",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("signup") }
                )
            }
        }
    }
}