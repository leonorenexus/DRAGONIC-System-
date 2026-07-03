package com.dragonic.system.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dragonic.system.ui.Screen
import com.dragonic.system.ui.components.*
import com.dragonic.system.ui.theme.*

@Composable
fun SetupScreen(navController: NavController) {
    val context = LocalContext.current
    var cameraGranted by remember { mutableStateOf(false) }
    var notifGranted by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { cameraGranted = it }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        DragonicTopBar(title = "INITIAL SETUP", subtitle = "Grant required permissions")
        Spacer(Modifier.height(24.dp))

        CyberCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("REQUIRED PERMISSIONS", color = TextSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))

            PermissionRow(
                icon = Icons.Default.CameraAlt,
                title = "Camera Access",
                desc = "Required for capturing intruder photos",
                granted = cameraGranted,
                onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )
            Spacer(Modifier.height(12.dp))
            PermissionRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                desc = "Alert you when intruder is detected",
                granted = notifGranted,
                onRequest = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        }

        Spacer(Modifier.height(24.dp))

        if (cameraGranted && notifGranted) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                DragonicButton(
                    text = "[ CONTINUE TO DASHBOARD ]",
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }

        Spacer(Modifier.weight(1f))
        DragonicFooter()
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    desc: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null,
            tint = if (granted) SuccessGreen else CyanNeon,
            modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 13.sp,
                fontFamily = FontFamily.Monospace)
            Text(desc, color = TextSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace)
        }
        if (granted) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Granted",
                tint = SuccessGreen, modifier = Modifier.size(20.dp))
        } else {
            TextButton(onClick = onRequest) {
                Text("GRANT", color = CyanNeon, fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
            }
        }
    }
}
