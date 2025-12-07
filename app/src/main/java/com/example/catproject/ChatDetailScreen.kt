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

    fun loadChat() {
        scope.launch {
            try {
                val newMsgs = RetrofitClient.instance.getChatDetail(myId, otherId)
                if (newMsgs.size > messages.size) {
                    messages = newMsgs
                    listState.animateScrollToItem(messages.size - 1)
                }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        while(true) { loadChat(); delay(2000) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.Bottom
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
                            color = if (isMe) Color(0xFFFF9800) else Color(0xFFF0F0F0), // Orange (Me) vs Abu (Dia)
                            shape = RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = if (isMe) 18.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 18.dp
                            ),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = msg.message,
                                color = if (isMe) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // INPUT AREA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(8.dp))
                if (text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val msgToSend = text
                            text = ""
                            scope.launch {
                                try {
                                    RetrofitClient.instance.sendMessage(SendMessageRequest(myId, otherId, msgToSend))
                                    loadChat()
                                } catch (e: Exception) {}
                            }
                        }
                    ) {
                        Text("Send", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}