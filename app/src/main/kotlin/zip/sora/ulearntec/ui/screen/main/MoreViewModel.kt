package zip.sora.ulearntec.ui.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.User

sealed interface MoreUiState {
    val user: User?

    data class Loading(
        override val user: User?
    ) : MoreUiState

    data class Success(
        override val user: User
    ) : MoreUiState

    data class Error(
        val message: (Context) -> String,
        override val user: User?,
    ) : MoreUiState
}

class MoreViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MoreUiState>(MoreUiState.Loading(null))
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update { MoreUiState.Loading(it.user) }
        viewModelScope.launch {
            val user = userRepository.refresh()
            if (user.isError()) {
                _uiState.update { MoreUiState.Error(user.error, it.user) }
                return@launch
            }

            _uiState.update { MoreUiState.Success(user.data) }
        }
    }

    suspend fun logout() {
        userRepository.logout()
    }

    init {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user.isError()) {
                _uiState.update { MoreUiState.Error(user.error, it.user) }
                return@launch
            }

            _uiState.update { MoreUiState.Success(user.data) }
        }
    }
}