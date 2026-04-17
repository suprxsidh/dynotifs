package com.suprasidh.dynotifs.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suprasidh.dynotifs.domain.model.NotificationAction
import com.suprasidh.dynotifs.domain.model.NotificationItem
import kotlinx.coroutines.delay

@Composable
fun IslandExpanded(
    item: NotificationItem,
    onCollapse: () -> Unit,
    onSendReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReply by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var remainingTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(item.chronometerBase) {
        item.chronometerBase?.let { base ->
            while (true) {
                val remaining = base - System.currentTimeMillis()
                if (remaining <= 0) break
                remainingTime = remaining
                delay(1000)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.icon != null) {
                        Image(
                            bitmap = item.icon.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (remainingTime > 0) {
                            Text(
                                text = formatTime(remainingTime),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                IconButton(onClick = onCollapse) {
                    Icon(Icons.Default.Close, "Collapse", tint = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = item.text,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f))

            if (item.remoteInput != null || item.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    item.actions.take(2).forEach { action ->
                        Text(
                            text = action.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    if (action.remoteInput != null) showReply = true
                                    else { action.intent?.send(); onCollapse() }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }

                if (showReply) {
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Reply", color = Color.White.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            onSendReply(replyText)
                            replyText = ""
                            showReply = false
                        }),
                        singleLine = true
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}