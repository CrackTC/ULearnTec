package zip.sora.ulearntec.ui.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.screen.main.HistoryUiState.*

sealed interface HistoryUiState {
    val allLivesWithHistory: List<Live>

    data class Loading(
        override val allLivesWithHistory: List<Live>
    ) : HistoryUiState

    data class Success(
        override val allLivesWithHistory: List<Live>
    ) : HistoryUiState

    data class Error(
        override val allLivesWithHistory: List<Live>,
        val message: (Context) -> String
    ) : HistoryUiState
}

class HistoryViewModel(
    private val liveRepository: LiveRepository,
    private val classRepository: ClassRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HistoryUiState>(Loading(emptyList()))
    val uiState = _uiState.asStateFlow()

    suspend fun fetchClass(classId: String): Class? {
        return classRepository.getClass(classId)
    }

    fun remove(live: Live) {
        viewModelScope.launch {
            if (live.history != null)
                liveRepository.removeHistory(live.history)
            refresh()
        }
    }

    // here we can only refresh locally
    fun refresh() {
        _uiState.update { Loading(it.allLivesWithHistory) }
        viewModelScope.launch {
            val lives = liveRepository.getAllLivesWithHistory()
            if (lives.isEmpty()) {
                _uiState.update { Error(emptyList()) { it.getString(R.string.no_history_yet) } }
                return@launch
            }

            _uiState.update { Success(lives) }
        }
    }

    init {
        refresh()
    }
}