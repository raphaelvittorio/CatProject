package com.example.catproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.catproject.network.ChatMessage
import com.example.catproject.network.RetrofitClient
import com.example.catproject.network.SendMessageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController, otherId: Int, otherName: String) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    val myId = UserSession.currentUser?.id ?: 0
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Fungsi Load Chat
    fun loadChat() {
        scope.launch {
            try {
                messages = RetrofitClient.instance.getChatDetail(myId, otherId)
                // Auto scroll ke pesan paling bawah
                if (messages.isNotEmpty()) {
                    listState.scrollToItem(messages.size - 1)
                }
            } catch (e: Exception) {}
        }
    }

    // Polling: Update otomatis setiap 3 detik
    LaunchedEffect(Unit) {
        while(true) {
            loadChat()
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherName, fontWeight = FontWeight.Bold) },
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
                .background(Color(0xFFF5F5F5))
        ) {
            // LIST PESAN
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                state = listState
            ) {
                items(messages) { msg ->
                    val isMe = (msg.sender_id == myId)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isMe) Color(0xFF3B82F6) else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = msg.message,
                                color = if (isMe) Color.White else Color.Black,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // INPUT BOX
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isNotEmpty()) {
                            val msgToSend = text
                            text = ""
                            scope.launch {
                                try {
                                    RetrofitClient.instance.sendMessage(SendMessageRequest(myId, otherId, msgToSend))
                                    loadChat()
                                } catch (e: Exception) {}
                            }
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF3B82F6), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
        }
    }
}