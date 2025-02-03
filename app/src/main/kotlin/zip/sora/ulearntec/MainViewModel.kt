package zip.sora.ulearntec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.Theme

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val theme: Theme) : MainUiState
}

class MainViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun setTheme(theme: Theme) {
        _uiState.update {
            MainUiState.Success(theme = theme)
        }
    }

    init {
        viewModelScope.launch {
            _uiState.update {
                MainUiState.Success(theme = preferenceRepository.getTheme())
            }
        }
    }
}