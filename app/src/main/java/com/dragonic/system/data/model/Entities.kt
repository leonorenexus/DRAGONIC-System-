package com.dragonic.system.data.model

import androidx.room.*

@Entity(tableName = "intruder_logs")
data class IntruderLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val photoPath: String,
    val triggerType: String, // "SCREEN_ON", "MOTION", "WRONG_PIN"
    val faceDetected: Boolean = false,
    val isOwner: Boolean = false,
    val confidence: Float = 0f
)

@Entity(tableName = "owner_faces")
data class OwnerFace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val enrolledAt: Long = System.currentTimeMillis(),
    val label: String = "Owner"
)

@Dao
interface IntruderLogDao {
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<IntruderLog>

    @Query("SELECT * FROM intruder_logs WHERE isOwner = 0 ORDER BY timestamp DESC")
    suspend fun getIntruderOnlyLogs(): List<IntruderLog>

    @Insert
    suspend fun insert(log: IntruderLog): Long

    @Delete
    suspend fun delete(log: IntruderLog)

    @Query("DELETE FROM intruder_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM intruder_logs WHERE isOwner = 0")
    suspend fun getIntruderCount(): Int
}

@Dao
interface OwnerFaceDao {
    @Query("SELECT * FROM owner_faces")
    suspend fun getAllFaces(): List<OwnerFace>

    @Insert
    suspend fun insert(face: OwnerFace): Long

    @Delete
    suspend fun delete(face: OwnerFace)

    @Query("DELETE FROM owner_faces")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM owner_faces")
    suspend fun getCount(): Int
}
