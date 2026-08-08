package com.liquidos.launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquidos.launcher.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    // Trạng thái hiển thị Control Center ảo
    private val _showControlCenter = MutableStateFlow(false)
    val showControlCenter: StateFlow<Boolean> = _showControlCenter

    // Trạng thái chụm 2 ngón tay (Pinch to zoom) để vào chế độ Edit
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    // Trạng thái Dynamic Island
    private val _dynamicIslandState = MutableStateFlow<IslandState>(IslandState.Idle)
    val dynamicIslandState: StateFlow<IslandState> = _dynamicIslandState

    // Lấy danh sách app từ Repository
    val allApps = appRepository.installedApps.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun toggleControlCenter(show: Boolean) {
        _showControlCenter.value = show
    }

    fun toggleEditMode(isEdit: Boolean) {
        _isEditMode.value = isEdit
    }

    fun triggerDynamicIsland(state: IslandState) {
        _dynamicIslandState.value = state
        // Tự động đóng sau 3 giây nếu là thông báo
        if (state is IslandState.Notification) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _dynamicIslandState.value = IslandState.Idle
            }
        }
    }
}

// Các trạng thái của Dynamic Island
sealed class IslandState {
    object Idle : IslandState()
    data class MusicPlaying(val songName: String, val artist: String, val progress: Float) : IslandState()
    data class Notification(val title: String, val message: String) : IslandState()
    object Expanding : IslandState()
}
