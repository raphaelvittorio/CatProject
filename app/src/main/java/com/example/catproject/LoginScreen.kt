package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catproject.network.LoginRequest
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CatPaw Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.login(LoginRequest(username, password))
                    if (res.status == "success") {
                        UserSession.currentUser = res.user // Simpan Session
                        Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
                        navController.navigate("main_app") { popUpTo("login") { inclusive = true } }
                    } else {
                        Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
    }
}