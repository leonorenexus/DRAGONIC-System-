package com.dragonic.system.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.dragonic.system.data.model.OwnerFace
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.sqrt

data class FaceAnalysisResult(
    val hasFace: Boolean,
    val isOwner: Boolean,
    val confidence: Float,
    val faceCount: Int
)

@Singleton
class FaceAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    suspend fun analyzeBitmap(
        bitmap: Bitmap,
        ownerFaces: List<OwnerFace>
    ): FaceAnalysisResult = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    cont.resume(FaceAnalysisResult(false, false, 0f, 0))
                    return@addOnSuccessListener
                }

                if (ownerFaces.isEmpty()) {
                    cont.resume(FaceAnalysisResult(true, false, 0f, faces.size))
                    return@addOnSuccessListener
                }

                // Compare detected face landmarks with owner faces
                val detectedFace = faces.first()
                var bestMatch = 0f

                ownerFaces.forEach { ownerFace ->
                    try {
                        val ownerBitmap = BitmapFactory.decodeFile(ownerFace.photoPath)
                        if (ownerBitmap != null) {
                            val similarity = compareSimpleSimilarity(bitmap, ownerBitmap)
                            if (similarity > bestMatch) bestMatch = similarity
                        }
                    } catch (e: Exception) { /* skip */ }
                }

                val isOwner = bestMatch > 0.55f
                cont.resume(
                    FaceAnalysisResult(
                        hasFace = true,
                        isOwner = isOwner,
                        confidence = bestMatch,
                        faceCount = faces.size
                    )
                )
            }
            .addOnFailureListener {
                cont.resume(FaceAnalysisResult(false, false, 0f, 0))
            }
    }

    // Simple pixel-based similarity (histogram comparison)
    private fun compareSimpleSimilarity(bmp1: Bitmap, bmp2: Bitmap): Float {
        val size = 32
        val scaled1 = Bitmap.createScaledBitmap(bmp1, size, size, true)
        val scaled2 = Bitmap.createScaledBitmap(bmp2, size, size, true)

        var diff = 0.0
        val total = size * size * 3.0

        for (x in 0 until size) {
            for (y in 0 until size) {
                val p1 = scaled1.getPixel(x, y)
                val p2 = scaled2.getPixel(x, y)
                diff += abs(((p1 shr 16) and 0xFF) - ((p2 shr 16) and 0xFF))
                diff += abs(((p1 shr 8) and 0xFF) - ((p2 shr 8) and 0xFF))
                diff += abs((p1 and 0xFF) - (p2 and 0xFF))
            }
        }

        val normalized = diff / (total * 255.0)
        return (1.0 - normalized).toFloat().coerceIn(0f, 1f)
    }

    suspend fun detectFaceOnly(bitmap: Bitmap): Boolean = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces -> cont.resume(faces.isNotEmpty()) }
            .addOnFailureListener { cont.resume(false) }
    }
}
