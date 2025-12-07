package com.example.catproject
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope(); val ctx = LocalContext.current

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CatPaw Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = u, onValueChange = { u = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.login(LoginRequest(u, p))
                    if (res.status == "success") {
                        UserSession.currentUser = res.user
                        navController.navigate("main_app") { popUpTo("login") { inclusive = true } }
                    } else Toast.makeText(ctx, "Login Gagal", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Log In") }
        Spacer(Modifier.height(16.dp))
        Text("Sign Up", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { navController.navigate("signup") })
    }
}