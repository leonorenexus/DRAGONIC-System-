package com.dragonic.system.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.Size
import androidx.core.app.NotificationCompat
import com.dragonic.system.DragonicApp
import com.dragonic.system.MainActivity
import com.dragonic.system.R
import com.dragonic.system.data.model.IntruderLog
import com.dragonic.system.data.model.OwnerFace
import com.dragonic.system.data.repository.DragonicRepository
import com.dragonic.system.ml.FaceAnalyzer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DragonicGuardService : Service() {

    @Inject lateinit var repository: DragonicRepository
    @Inject lateinit var faceAnalyzer: FaceAnalyzer

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private var captureInProgress = false

    companion object {
        const val ACTION_CAPTURE = "com.dragonic.system.CAPTURE"
        const val ACTION_STOP = "com.dragonic.system.STOP"
        const val NOTIF_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, DragonicGuardService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, DragonicGuardService::class.java)
                .setAction(ACTION_STOP)
            context.startService(intent)
        }

        fun triggerCapture(context: Context) {
            val intent = Intent(context, DragonicGuardService::class.java)
                .setAction(ACTION_CAPTURE)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification("DRAGONIC Guard Active"))
        cameraManager = getSystemService(CameraManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            ACTION_CAPTURE -> serviceScope.launch { captureAndAnalyze("MANUAL") }
        }
        return START_STICKY
    }

    private suspend fun captureAndAnalyze(triggerType: String) {
        if (captureInProgress) return
        captureInProgress = true
        try {
            val bitmap = captureFromFrontCamera() ?: return
            val photoPath = saveBitmap(bitmap)
            val ownerFaces = repository.getAllFaces()
            val result = faceAnalyzer.analyzeBitmap(bitmap, ownerFaces)

            val log = IntruderLog(
                photoPath = photoPath,
                triggerType = triggerType,
                faceDetected = result.hasFace,
                isOwner = result.isOwner,
                confidence = result.confidence
            )
            repository.insertLog(log)

            if (!result.isOwner && result.hasFace) {
                showIntruderAlert()
            }
        } finally {
            captureInProgress = false
        }
    }

    private suspend fun captureFromFrontCamera(): Bitmap? = suspendCancellableCoroutine { cont ->
        try {
            val frontCameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager?.getCameraCharacteristics(id)
                    ?.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: run { cont.resume(null); return@suspendCancellableCoroutine }

            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)

            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    cont.resume(bitmap)
                } finally {
                    image.close()
                    cameraDevice?.close()
                    cameraDevice = null
                }
            }, Handler(Looper.getMainLooper()))

            cameraManager?.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val surface = imageReader!!.surface
                    camera.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                    .apply { addTarget(surface) }
                                    .build()
                                session.capture(request, null, null)
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                cont.resume(null)
                            }
                        }, null
                    )
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close(); cont.resume(null) }
                override fun onError(camera: CameraDevice, error: Int) { camera.close(); cont.resume(null) }
            }, Handler(Looper.getMainLooper()))

        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    private fun saveBitmap(bitmap: Bitmap): String {
        val dir = File(filesDir, "captures").also { it.mkdirs() }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "DRAGONIC_$ts.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return file.absolutePath
    }

    private fun showIntruderAlert() {
        val notif = NotificationCompat.Builder(this, DragonicApp.CHANNEL_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠ DRAGONIC ALERT")
            .setContentText("Unknown face detected on your device!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        getSystemService(NotificationManager::class.java).notify(2001, notif)
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, DragonicApp.CHANNEL_GUARD)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("DRAGONIC System")
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        serviceScope.cancel()
        cameraDevice?.close()
        imageReader?.close()
        super.onDestroy()
    }
}
