package com.example.catproject
import android.widget.Toast
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
fun SignUpScreen(navController: NavController) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var e by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope(); val ctx = LocalContext.current

    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = u, onValueChange = { u = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.signup(SignupRequest(u, p, e))
                    if (res.status == "success") { Toast.makeText(ctx, "Success! Login now.", Toast.LENGTH_SHORT).show(); navController.popBackStack() }
                    else Toast.makeText(ctx, res.message, Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) { Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show() }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Sign Up") }
    }
}