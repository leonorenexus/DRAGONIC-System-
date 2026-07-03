package com.dragonic.system.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.dragonic.system.data.model.IntruderLog
import com.dragonic.system.data.repository.DragonicRepository
import com.dragonic.system.ui.components.*
import com.dragonic.system.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: DragonicRepository
) : ViewModel() {
    private val _logs = MutableStateFlow<List<IntruderLog>>(emptyList())
    val logs: StateFlow<List<IntruderLog>> = _logs.asStateFlow()

    private val _showIntruderOnly = MutableStateFlow(false)
    val showIntruderOnly: StateFlow<Boolean> = _showIntruderOnly.asStateFlow()

    init { loadLogs() }

    fun loadLogs() {
        viewModelScope.launch {
            _logs.value = if (_showIntruderOnly.value)
                repository.getIntruderLogs()
            else
                repository.getAllLogs()
        }
    }

    fun toggleFilter() {
        _showIntruderOnly.value = !_showIntruderOnly.value
        loadLogs()
    }

    fun deleteLog(log: IntruderLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
            loadLogs()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllLogs()
            loadLogs()
        }
    }
}

@Composable
fun LogsScreen(navController: NavController, viewModel: LogsViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    val showIntruderOnly by viewModel.showIntruderOnly.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        DragonicTopBar(
            title = "CAPTURE LOGS",
            subtitle = "${logs.size} records"
        )

        // Filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DragonicButton(
                text = if (showIntruderOnly) "ALL LOGS" else "INTRUDERS ONLY",
                onClick = { viewModel.toggleFilter() },
                color = if (showIntruderOnly) DangerRed else CyanNeon
            )
            IconButton(onClick = { showClearDialog = true }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all", tint = DangerRed)
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null,
                        tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("NO LOGS FOUND", color = TextSecondary,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogCard(log = log, onDelete = { viewModel.deleteLog(log) })
                }
            }
        }

        DragonicFooter()
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CardSurface,
            title = {
                Text("CLEAR ALL LOGS", color = DangerRed,
                    fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            },
            text = {
                Text("Delete all capture logs? This cannot be undone.",
                    color = TextPrimary, fontFamily = FontFamily.Monospace)
            },
            confirmButton = {
                DragonicButton("CONFIRM", onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                }, color = DangerRed)
            },
            dismissButton = {
                DragonicButton("CANCEL", onClick = { showClearDialog = false })
            }
        )
    }
}

@Composable
private fun LogCard(log: IntruderLog, onDelete: () -> Unit) {
    val df = SimpleDateFormat("dd MMM yyyy  HH:mm:ss", Locale.getDefault())
    val timeStr = df.format(Date(log.timestamp))
    val statusColor = when {
        log.isOwner -> SuccessGreen
        log.faceDetected -> DangerRed
        else -> WarnYellow
    }
    val statusLabel = when {
        log.isOwner -> "OWNER"
        log.faceDetected -> "INTRUDER"
        else -> "NO FACE"
    }

    CyberCard(glowColor = statusColor) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail
            val bitmap = remember(log.photoPath) {
                try { BitmapFactory.decodeFile(log.photoPath)?.asImageBitmap() } catch (e: Exception) { null }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Capture",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(DarkSurface, RoundedCornerShape(4.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BrokenImage, contentDescription = null,
                        tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                            tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
                Text(text = timeStr, color = TextSecondary, fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace)
                Text(text = "TRIGGER: ${log.triggerType}", color = TextSecondary,
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                if (log.faceDetected) {
                    Text(
                        text = "CONFIDENCE: ${(log.confidence * 100).toInt()}%",
                        color = statusColor.copy(alpha = 0.8f),
                        fontSize = 10.sp, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
