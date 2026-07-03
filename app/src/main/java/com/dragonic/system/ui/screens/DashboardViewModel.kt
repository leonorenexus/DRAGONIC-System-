package com.dragonic.system.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dragonic.system.data.repository.DragonicRepository
import com.dragonic.system.service.DragonicGuardService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isGuardActive: Boolean = false,
    val isFaceEnrolled: Boolean = false,
    val intruderCount: Int = 0,
    val totalLogs: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DragonicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.isGuardEnabled,
                repository.isFaceEnrolled
            ) { guard, face -> guard to face }
                .collect { (guard, face) ->
                    val intruderCount = repository.getIntruderCount()
                    val allLogs = repository.getAllLogs().size
                    _uiState.update {
                        it.copy(
                            isGuardActive = guard,
                            isFaceEnrolled = face,
                            intruderCount = intruderCount,
                            totalLogs = allLogs
                        )
                    }
                }
        }
    }

    fun toggleGuard() {
        viewModelScope.launch {
            val current = _uiState.value.isGuardActive
            val newState = !current
            repository.setGuardEnabled(newState)
            if (newState) {
                DragonicGuardService.start(context)
            } else {
                DragonicGuardService.stop(context)
            }
            // Save for boot receiver
            context.getSharedPreferences("dragonic_guard", Context.MODE_PRIVATE)
                .edit().putBoolean("guard_was_active", newState).apply()
        }
    }

    fun triggerManualCapture() {
        DragonicGuardService.triggerCapture(context)
    }
}
