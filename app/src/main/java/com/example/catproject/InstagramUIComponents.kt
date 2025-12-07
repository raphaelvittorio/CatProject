package com.example.catproject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage

val InstaGradient = Brush.linearGradient(colors = listOf(Color(0xFFFBAA47), Color(0xFFD91A46), Color(0xFFA60F93)))

@Composable
fun StoryItem(imageUrl: String, username: String, isMe: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(if(isMe) SolidColor(Color.Transparent) else InstaGradient).padding(3.dp).clip(CircleShape).background(Color.White).padding(3.dp)) {
            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = if(isMe) "Your Story" else username, fontSize = 11.sp)
    }
}