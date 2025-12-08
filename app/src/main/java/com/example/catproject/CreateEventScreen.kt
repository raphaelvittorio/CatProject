package com.example.catproject

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Title
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
import com.example.catproject.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(navController: NavController) {
    // State Form
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // State Date & Time
    var selectedDateStr by remember { mutableStateOf("") }
    var selectedTimeStr by remember { mutableStateOf("") }

    // State Dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    // Date Picker State
    val dateState = rememberDatePickerState()

    // Time Picker State
    val timeState = rememberTimePickerState()

    // --- LOGIC FORMAT TANGGAL ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    dateState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDateStr = formatter.format(Date(millis))
                    }
                }) { Text("OK", color = Color(0xFFFF9800)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // --- LOGIC FORMAT WAKTU ---
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val hour = timeState.hour
                    val minute = timeState.minute
                    selectedTimeStr = String.format("%02d:%02d", hour, minute)
                }) { Text("OK", color = Color(0xFFFF9800)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Time", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = timeState)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Event", fontWeight = FontWeight.Bold) },
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
            // --- 1. COVER IMAGE PICKER (HERO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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
                    // Overlay Change Text
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Change Cover",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Add Event Cover Photo", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // --- 2. FORM FIELDS ---
            Column(modifier = Modifier.padding(20.dp)) {

                Text("Event Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(Modifier.height(16.dp))

                // Title
                ModernTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Event Title",
                    icon = Icons.Rounded.Title
                )

                Spacer(Modifier.height(12.dp))

                // Date & Time Row
                Row(Modifier.fillMaxWidth()) {
                    // Date Picker Trigger
                    Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                        ModernTextField(
                            value = selectedDateStr,
                            onValueChange = {},
                            label = "Date",
                            icon = Icons.Rounded.CalendarToday,
                            readOnly = true,
                            enabled = false // Disable typing, enable click
                        )
                        // Invisible overlay to catch click since enabled=false disables click on TextField
                        Box(Modifier.matchParentSize().clickable { showDatePicker = true })
                    }

                    Spacer(Modifier.width(12.dp))

                    // Time Picker Trigger
                    Box(modifier = Modifier.weight(1f)) {
                        ModernTextField(
                            value = selectedTimeStr,
                            onValueChange = {},
                            label = "Time",
                            icon = Icons.Rounded.Schedule,
                            readOnly = true,
                            enabled = false
                        )
                        Box(Modifier.matchParentSize().clickable { showTimePicker = true })
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Location
                ModernTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    icon = Icons.Rounded.LocationOn
                )

                Spacer(Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFFF9800),
                        unfocusedIndicatorColor = Color.LightGray
                    ),
                    leadingIcon = {
                        Icon(Icons.Rounded.Description, null, tint = Color.Gray)
                    }
                )

                Spacer(Modifier.height(32.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (title.isNotEmpty() && selectedDateStr.isNotEmpty() && imageUri != null) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val file = File(context.cacheDir, "event_img.jpg")
                                    context.contentResolver.openInputStream(imageUri!!)?.copyTo(FileOutputStream(file))

                                    val imgBody = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                                    val tBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val dBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val locBody = location.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val dateBody = selectedDateStr.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val timeBody = selectedTimeStr.toRequestBody("text/plain".toMediaTypeOrNull())

                                    val res = RetrofitClient.instance.createEvent(tBody, dBody, dateBody, timeBody, locBody, imgBody)

                                    if (res.status == "success") {
                                        Toast.makeText(context, "Event Created!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Failed: ${res.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally { isLoading = false }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields & photo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Publish Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper Composable untuk TextField Seragam
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    enabled: Boolean = true
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
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFFF9800),
            unfocusedIndicatorColor = Color.LightGray,
            disabledIndicatorColor = Color.LightGray,
            disabledTextColor = Color.Black,
            disabledLabelColor = Color.Gray,
            disabledLeadingIconColor = Color.Gray
        ),
        singleLine = true,
        readOnly = readOnly,
        enabled = enabled
    )
}