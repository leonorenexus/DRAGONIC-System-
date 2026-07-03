package com.dragonic.system.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.dragonic.system.data.dataStore
import com.dragonic.system.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DragonicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intruderLogDao: IntruderLogDao,
    private val ownerFaceDao: OwnerFaceDao
) {
    companion object {
        val KEY_GUARD_ENABLED = booleanPreferencesKey("guard_enabled")
        val KEY_FACE_ENROLLED = booleanPreferencesKey("face_enrolled")
        val KEY_SENSITIVITY = intPreferencesKey("sensitivity")
    }

    val isGuardEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_GUARD_ENABLED] ?: false
    }

    val isFaceEnrolled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_FACE_ENROLLED] ?: false
    }

    val sensitivity: Flow<Int> = context.dataStore.data.map {
        it[KEY_SENSITIVITY] ?: 2
    }

    suspend fun setGuardEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_GUARD_ENABLED] = enabled }
    }

    suspend fun setFaceEnrolled(enrolled: Boolean) {
        context.dataStore.edit { it[KEY_FACE_ENROLLED] = enrolled }
    }

    suspend fun setSensitivity(level: Int) {
        context.dataStore.edit { it[KEY_SENSITIVITY] = level }
    }

    suspend fun getAllLogs() = intruderLogDao.getAllLogs()
    suspend fun getIntruderLogs() = intruderLogDao.getIntruderOnlyLogs()
    suspend fun insertLog(log: IntruderLog) = intruderLogDao.insert(log)
    suspend fun deleteLog(log: IntruderLog) = intruderLogDao.delete(log)
    suspend fun clearAllLogs() = intruderLogDao.deleteAll()
    suspend fun getIntruderCount() = intruderLogDao.getIntruderCount()

    suspend fun getAllFaces() = ownerFaceDao.getAllFaces()
    suspend fun insertFace(face: OwnerFace) = ownerFaceDao.insert(face)
    suspend fun deleteFace(face: OwnerFace) = ownerFaceDao.delete(face)
    suspend fun clearAllFaces() = ownerFaceDao.deleteAll()
    suspend fun getFaceCount() = ownerFaceDao.getCount()
}
