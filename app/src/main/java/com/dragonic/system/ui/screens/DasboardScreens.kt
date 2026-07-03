package com.dragonic.system.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dragonic.system.ui.Screen
import com.dragonic.system.ui.components.*
import com.dragonic.system.ui.theme.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "dragon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "rot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        DragonicTopBar(
            title = "DRAGONIC SYSTEM",
            subtitle = "v1.0.0 · Security Protocol Active"
        )

        // Main status orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(rotation)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(CyanNeon, PurpleNeon, Color.Transparent, CyanNeon)
                        ),
                        shape = CircleShape
                    )
            )
            // Inner orb
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (state.isGuardActive)
                                listOf(CyanNeon.copy(alpha = 0.3f), DeepBlack)
                            else
                                listOf(PurpleNeon.copy(alpha = 0.1f), DeepBlack)
                        ),
                        shape = CircleShape
                    )
                    .border(
                        2.dp,
                        if (state.isGuardActive) CyanNeon.copy(alpha = 0.6f) else TextSecondary.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (state.isGuardActive) Icons.Default.Shield else Icons.Default.ShieldMoon,
                        contentDescription = null,
                        tint = if (state.isGuardActive) CyanNeon else TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.isGuardActive) "ACTIVE" else "OFFLINE",
                        color = if (state.isGuardActive) CyanNeon else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Toggle guard button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            DragonicButton(
                text = if (state.isGuardActive) "[ DEACTIVATE GUARD ]" else "[ ACTIVATE GUARD ]",
                onClick = { viewModel.toggleGuard() },
                color = if (state.isGuardActive) DangerRed else CyanNeon,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "INTRUDERS",
                value = state.intruderCount.toString(),
                color = if (state.intruderCount > 0) DangerRed else SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TOTAL LOGS",
                value = state.totalLogs.toString(),
                color = CyanNeon,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Status indicators
        CyberCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "SYSTEM STATUS",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            StatusIndicator(state.isGuardActive, "Guard Service")
            Spacer(Modifier.height(8.dp))
            StatusIndicator(state.isFaceEnrolled, "Face ID Enrolled")
            Spacer(Modifier.height(8.dp))
            StatusIndicator(true, "Database Online")
        }

        Spacer(Modifier.height(12.dp))

        // Navigation grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NavButton(
                    icon = Icons.Default.Face,
                    label = "FACE ID",
                    onClick = { navController.navigate(Screen.FaceEnroll.route) },
                    modifier = Modifier.weight(1f)
                )
                NavButton(
                    icon = Icons.Default.PhotoLibrary,
                    label = "LOGS",
                    onClick = { navController.navigate(Screen.Logs.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NavButton(
                    icon = Icons.Default.CameraAlt,
                    label = "CAPTURE",
                    onClick = { viewModel.triggerManualCapture() },
                    modifier = Modifier.weight(1f),
                    color = PurpleNeon
                )
                NavButton(
                    icon = Icons.Default.Settings,
                    label = "SETTINGS",
                    onClick = { navController.navigate(Screen.Settings.route) },
                    modifier = Modifier.weight(1f),
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.weight(1f))
        DragonicFooter(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    CyberCard(modifier = modifier, glowColor = color) {
        Text(
            text = value,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CyanNeon
) {
    CyberCard(modifier = modifier.clickable { onClick() }, glowColor = color) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}
