package com.example.catproject
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.catproject.network.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(postId: Int) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var txt by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope(); val ctx = LocalContext.current

    fun load() { scope.launch { try { comments = RetrofitClient.instance.getComments(postId) } catch (e: Exception){} } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Comments", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        LazyColumn(Modifier.weight(1f)) {
            items(comments) { c ->
                Row(Modifier.padding(vertical = 8.dp)) {
                    val pp = if(c.profile_picture_url!=null) "http://10.0.2.2/catpaw_api/uploads/${c.profile_picture_url}" else "https://via.placeholder.com/150"
                    Image(rememberAsyncImagePainter(pp), null, Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(8.dp))
                    Column { Text(c.username, fontWeight = FontWeight.Bold); Text(c.comment) }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = txt,
                onValueChange = { txt = it },
                placeholder = { Text("Comment...") },
                modifier = Modifier.weight(1f),
                // PERBAIKAN DISINI: Ganti textFieldColors -> colors
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {
                if(txt.isNotEmpty()) {
                    scope.launch {
                        if(RetrofitClient.instance.addComment(CommentRequest(UserSession.currentUser!!.id, postId, txt)).status=="success") {
                            txt = ""; load()
                        }
                    }
                }
            }) { Icon(Icons.Default.Send, null) }
        }
    }
}