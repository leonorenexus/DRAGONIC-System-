package com.dragonic.system.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.dragonic.system.data.repository.DragonicRepository
import com.dragonic.system.ui.components.*
import com.dragonic.system.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DragonicRepository
) : ViewModel() {
    val sensitivity = repository.sensitivity.stateIn(viewModelScope, SharingStarted.Eagerly, 2)
    val isFaceEnrolled = repository.isFaceEnrolled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setSensitivity(level: Int) { viewModelScope.launch { repository.setSensitivity(level) } }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val faceEnrolled by viewModel.isFaceEnrolled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        DragonicTopBar(title = "SETTINGS", subtitle = "System configuration")

        Spacer(Modifier.height(16.dp))

        // Sensitivity
        CyberCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("DETECTION SENSITIVITY", color = TextSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(1 to "LOW", 2 to "MED", 3 to "HIGH").forEach { (level, label) ->
                    val selected = sensitivity == level
                    DragonicButton(
                        text = label,
                        onClick = { viewModel.setSensitivity(level) },
                        color = if (selected) CyanNeon else TextSecondary,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (sensitivity) {
                    1 -> "Low: Capture only when screen is unlocked"
                    3 -> "High: Capture on screen on + motion + PIN fail"
                    else -> "Medium: Capture on screen on + PIN fail"
                },
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 10.sp, fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(12.dp))

        // Face ID status
        CyberCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FACE ID STATUS", color = TextSecondary, fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                    Spacer(Modifier.height(4.dp))
                    StatusIndicator(faceEnrolled, if (faceEnrolled) "Face enrolled" else "No face enrolled")
                }
                Icon(
                    Icons.Default.FaceRetouchingNatural,
                    contentDescription = null,
                    tint = if (faceEnrolled) SuccessGreen else TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // About
        CyberCard(modifier = Modifier.padding(horizontal = 16.dp), glowColor = PurpleNeon) {
            Text("ABOUT", color = TextSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))

            InfoRow("App", "DRAGONIC System")
            InfoRow("Version", "1.0.0")
            InfoRow("Developer", "Pai Leonore")
            InfoRow("Studio", "Leonore Tech Team")
            InfoRow("Engine", "ML Kit Face Detection")
            InfoRow("Build", "Kotlin + Jetpack Compose")
        }

        Spacer(Modifier.height(16.dp))
        DragonicFooter()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = TextPrimary, fontSize = 11.sp,
            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
