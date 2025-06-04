package zip.sora.ulearntec.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.PlayerTheme
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.SwipeSeekMode
import zip.sora.ulearntec.domain.Theme

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val dataExpireMillis: Long,
        val maxPlayerCacheMb: Long,
        val theme: Theme,
        val playerTheme: PlayerTheme,
        val swipeSeekMode: SwipeSeekMode,
        val swipeSeekFixedMillis: Long,
        val swipeSeekPercent: Float,
        val swipeVolumePercent: Float,
        val swipeBrightnessPercent: Float,
        val longPressSpeed: Float
    ) : SettingsUiState
}

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                SettingsUiState.Success(
                    preferenceRepository.getDataExpireMillis(),
                    preferenceRepository.getMaxPlayerCacheMb(),
                    preferenceRepository.getTheme(),
                    preferenceRepository.getPlayerTheme(),
                    preferenceRepository.getSwipeSeekMode(),
                    preferenceRepository.getSwipeSeekFixedMillis(),
                    preferenceRepository.getSwipeSeekPercent(),
                    preferenceRepository.getSwipeVolumePercent(),
                    preferenceRepository.getSwipeBrightnessPercent(),
                    preferenceRepository.getLongPressSpeed()
                )
            }
        }
    }

    fun setDataExpireMillis(millis: Long) {
        viewModelScope.launch {
            preferenceRepository.setDataExpireMillis(millis)
            _uiState.update {
                it as SettingsUiState.Success
                it.copy(dataExpireMillis = millis)
            }
        }
    }

    fun setMaxPlayerCacheMb(mb: Long) {
        viewModelScope.launch {
            preferenceRepository.setMaxPlayerCacheMb(mb)
            _uiState.update {
                it as SettingsUiState.Success
                it.copy(maxPlayerCacheMb = mb)
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            preferenceRepository.setTheme(theme)
            _uiState.update {
                it as SettingsUiState.Success
                it.copy(theme = theme)
            }
        }
    }

    fun setPlayerTheme(theme: PlayerTheme) {
        viewModelScope.launch {
            preferenceRepository.setPlayerTheme(theme)
            _uiState.update {
                it as SettingsUiState.Success
                it.copy(playerTheme = theme)
            }
        }
    }

    fun setSwipeSeekMode(mode: SwipeSeekMode) {
        viewModelScope.launch {
            preferenceRepository.setSwipeSeekMode(mode)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(swipeSeekMode = mode)
            }
        }
    }

    fun setSwipeSeekFixedMillis(millis: Long) {
        viewModelScope.launch {
            preferenceRepository.setSwipeSeekFixedMillis(millis)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(swipeSeekFixedMillis = millis)
            }
        }
    }

    fun setSwipeSeekPercent(percent: Float) {
        viewModelScope.launch {
            preferenceRepository.setSwipeSeekPercent(percent)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(swipeSeekPercent = percent)
            }
        }
    }

    fun setSwipeVolumePercent(percent: Float) {
        viewModelScope.launch {
            preferenceRepository.setSwipeVolumePercent(percent)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(swipeVolumePercent = percent)
            }
        }
    }

    fun setSwipeBrightnessPercent(percent: Float) {
        viewModelScope.launch {
            preferenceRepository.setSwipeBrightnessPercent(percent)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(swipeBrightnessPercent = percent)
            }
        }
    }

    fun setLongPressSpeed(speed: Float) {
        viewModelScope.launch {
            preferenceRepository.setLongPressSpeed(speed)

            _uiState.update {
                it as SettingsUiState.Success
                it.copy(longPressSpeed = speed)
            }
        }
    }
}