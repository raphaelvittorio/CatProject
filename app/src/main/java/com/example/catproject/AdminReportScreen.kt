package com.example.catproject

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.AdminResolveReportRequest
import com.example.catproject.network.ReportItem
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportScreen(navController: NavController) {
    var reports by remember { mutableStateOf<List<ReportItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadReports() {
        scope.launch {
            try {
                reports = RetrofitClient.instance.getAdminReports()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading reports", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadReports() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Reports") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.ArrowBack, null) } }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (reports.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No pending reports.", color = Color.Gray) }
        } else {
            LazyColumn(contentPadding = p) {
                items(reports) { report ->
                    ReportCard(report, onAction = { loadReports() })
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: ReportItem, onAction: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("REPORTED", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("by ${report.reporter_name}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))
            Text("Reason: ${report.reason}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            // Content Preview (Jika Post)
            if (!report.post_image.isNullOrEmpty()) {
                Row(Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter("http://10.0.2.2/catpaw_api/uploads/${report.post_image}"),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(report.post_caption ?: "No Caption", fontSize = 12.sp, maxLines = 3, color = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                // DISMISS BUTTON
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            RetrofitClient.instance.resolveReport(AdminResolveReportRequest(report.id, "dismiss"))
                            onAction()
                            Toast.makeText(context, "Report Ignored", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Keep Content")
                }

                Spacer(Modifier.width(8.dp))

                // DELETE CONTENT BUTTON
                Button(
                    onClick = {
                        scope.launch {
                            RetrofitClient.instance.resolveReport(AdminResolveReportRequest(report.id, "delete_content"))
                            onAction()
                            Toast.makeText(context, "Content Deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Content")
                }
            }
        }
    }
}