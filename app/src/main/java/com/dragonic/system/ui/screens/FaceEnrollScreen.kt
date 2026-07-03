package com.dragonic.system.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.dragonic.system.data.model.OwnerFace
import com.dragonic.system.data.repository.DragonicRepository
import com.dragonic.system.ml.FaceAnalyzer
import com.dragonic.system.ui.components.*
import com.dragonic.system.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume

data class FaceEnrollState(
    val faces: List<OwnerFace> = emptyList(),
    val isCapturing: Boolean = false,
    val lastCaptured: Bitmap? = null,
    val message: String = "",
    val messageType: String = "info" // info, success, error
)

@HiltViewModel
class FaceEnrollViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DragonicRepository,
    private val faceAnalyzer: FaceAnalyzer
) : ViewModel() {

    private val _state = MutableStateFlow(FaceEnrollState())
    val state: StateFlow<FaceEnrollState> = _state.asStateFlow()

    init { loadFaces() }

    private fun loadFaces() {
        viewModelScope.launch {
            _state.value = _state.value.copy(faces = repository.getAllFaces())
        }
    }

    fun captureFace() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCapturing = true, message = "Capturing...")
            try {
                val bitmap = captureFromFrontCamera(context)
                if (bitmap == null) {
                    _state.value = _state.value.copy(
                        isCapturing = false, message = "Camera failed. Try again.", messageType = "error"
                    )
                    return@launch
                }

                val hasFace = faceAnalyzer.detectFaceOnly(bitmap)
                if (!hasFace) {
                    _state.value = _state.value.copy(
                        isCapturing = false, message = "No face detected. Look at the camera.", messageType = "error"
                    )
                    return@launch
                }

                val path = saveFaceBitmap(bitmap)
                val ownerFace = OwnerFace(photoPath = path)
                repository.insertFace(ownerFace)
                repository.setFaceEnrolled(true)

                _state.value = _state.value.copy(
                    isCapturing = false,
                    lastCaptured = bitmap,
                    message = "Face enrolled successfully!",
                    messageType = "success",
                    faces = repository.getAllFaces()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCapturing = false,
                    message = "Error: ${e.message}",
                    messageType = "error"
                )
            }
        }
    }

    fun deleteFace(face: OwnerFace) {
        viewModelScope.launch {
            repository.deleteFace(face)
            if (repository.getFaceCount() == 0) {
                repository.setFaceEnrolled(false)
            }
            _state.value = _state.value.copy(faces = repository.getAllFaces())
        }
    }

    fun clearAllFaces() {
        viewModelScope.launch {
            repository.clearAllFaces()
            repository.setFaceEnrolled(false)
            _state.value = _state.value.copy(faces = emptyList(), message = "All faces cleared.")
        }
    }

    private suspend fun captureFromFrontCamera(context: Context): Bitmap? =
        suspendCancellableCoroutine { cont ->
            try {
                val manager = context.getSystemService(CameraManager::class.java)
                val frontId = manager.cameraIdList.firstOrNull { id ->
                    manager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                } ?: run { cont.resume(null); return@suspendCancellableCoroutine }

                val reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
                reader.setOnImageAvailableListener({ r ->
                    val img = r.acquireLatestImage()
                    try {
                        val buf = img.planes[0].buffer
                        val bytes = ByteArray(buf.remaining())
                        buf.get(bytes)
                        cont.resume(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    } finally { img.close() }
                }, Handler(Looper.getMainLooper()))

                manager.openCamera(frontId, object : CameraDevice.StateCallback() {
                    override fun onOpened(cam: CameraDevice) {
                        cam.createCaptureSession(listOf(reader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(sess: CameraCaptureSession) {
                                    val req = cam.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                        .apply { addTarget(reader.surface) }.build()
                                    sess.capture(req, null, null)
                                }
                                override fun onConfigureFailed(sess: CameraCaptureSession) { cont.resume(null) }
                            }, null)
                    }
                    override fun onDisconnected(cam: CameraDevice) { cam.close(); cont.resume(null) }
                    override fun onError(cam: CameraDevice, e: Int) { cam.close(); cont.resume(null) }
                }, Handler(Looper.getMainLooper()))
            } catch (e: Exception) { cont.resume(null) }
        }

    private fun saveFaceBitmap(bitmap: Bitmap): String {
        val dir = File(context.filesDir, "faces").also { it.mkdirs() }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "face_$ts.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return file.absolutePath
    }
}

@Composable
fun FaceEnrollScreen(
    navController: NavController,
    viewModel: FaceEnrollViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .verticalScroll(rememberScrollState())
    ) {
        DragonicTopBar(title = "FACE ID SETUP", subtitle = "${state.faces.size} face(s) enrolled")

        Spacer(Modifier.height(16.dp))

        // Scan frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurface)
                .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (state.lastCaptured != null) {
                Image(
                    bitmap = state.lastCaptured!!.asImageBitmap(),
                    contentDescription = "Captured face",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                )
                ScanLineOverlay(modifier = Modifier.fillMaxSize())
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FaceRetouchingNatural, contentDescription = null,
                        tint = CyanNeon.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("POSITION YOUR FACE HERE", color = TextSecondary,
                        fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                }
            }

            // Corner overlays
            CornerOverlay()
        }

        Spacer(Modifier.height(16.dp))

        // Status message
        if (state.message.isNotEmpty()) {
            val msgColor = when (state.messageType) {
                "success" -> SuccessGreen
                "error" -> DangerRed
                else -> CyanNeon
            }
            Text(
                text = state.message,
                color = msgColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        // Capture button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (state.isCapturing) {
                CircularProgressIndicator(color = CyanNeon, modifier = Modifier.size(32.dp))
            } else {
                DragonicButton(
                    text = "[ ENROLL FACE ]",
                    onClick = { viewModel.captureFace() },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Enrolled faces gallery
        if (state.faces.isNotEmpty()) {
            CyberCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ENROLLED FACES", color = TextSecondary, fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                    TextButton(onClick = { viewModel.clearAllFaces() }) {
                        Text("CLEAR ALL", color = DangerRed, fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.faces) { face ->
                        Box {
                            val bmp = remember(face.photoPath) {
                                try { BitmapFactory.decodeFile(face.photoPath)?.asImageBitmap() } catch (e: Exception) { null }
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp,
                                    contentDescription = "Face",
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteFace(face) },
                                modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = "Remove",
                                    tint = DangerRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        CyberCard(modifier = Modifier.padding(horizontal = 16.dp), glowColor = WarnYellow) {
            Row {
                Icon(Icons.Default.Info, contentDescription = null,
                    tint = WarnYellow, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Enroll 3-5 photos in different lighting conditions for best accuracy.",
                    color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        DragonicFooter()
    }
}

@Composable
private fun CornerOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        // TL
        Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawLine(color = androidx.compose.ui.graphics.Color(0xFF00FFFF),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(24.dp.toPx(), 0f), strokeWidth = 2.dp.toPx())
                drawLine(color = androidx.compose.ui.graphics.Color(0xFF00FFFF),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, 24.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
        }
        // TR
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawLine(color = androidx.compose.ui.graphics.Color(0xFF00FFFF),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(24.dp.toPx(), 0f), strokeWidth = 2.dp.toPx())
                drawLine(color = androidx.compose.ui.graphics.Color(0xFF00FFFF),
                    start = androidx.compose.ui.geometry.Offset(24.dp.toPx(), 0f),
                    end = androidx.compose.ui.geometry.Offset(24.dp.toPx(), 24.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.Canvas(
    modifier: Modifier,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {}

@Composable
private fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}
