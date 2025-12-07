package com.example.catproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen() {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        // Search Bar
        Box(Modifier.padding(8.dp)) {
            TextField(
                value = "", onValueChange = {},
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                // PERBAIKAN DISINI: Ganti textFieldColors -> colors
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedContainerColor = Color(0xFFEFEFEF),
                    disabledContainerColor = Color(0xFFEFEFEF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium
            )
        }
        // Grid Random
        LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            items(30) {
                Image(
                    painter = rememberAsyncImagePainter("https://placekitten.com/200/200?image=$it"),
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ReelsScreen() {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text("Reels Video Placeholder", color = Color.White)
    }
}