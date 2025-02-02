package zip.sora.ulearntec.ui.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.User

sealed interface AccountUiState {
    val user: User?

    data class Loading(
        override val user: User?
    ) : AccountUiState

    data class Success(
        override val user: User
    ) : AccountUiState

    data class Error(
        val message: (Context) -> String,
        override val user: User?,
    ) : AccountUiState
}

class AccountViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading(null))
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update { AccountUiState.Loading(it.user) }
        viewModelScope.launch {
            val user = userRepository.refresh()
            if (user.isError()) {
                _uiState.update { AccountUiState.Error(user.error, it.user) }
                return@launch
            }

            _uiState.update { AccountUiState.Success(user.data) }
        }
    }

    suspend fun logout() {
        userRepository.logout()
    }

    init {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user.isError()) {
                _uiState.update { AccountUiState.Error(user.error, it.user) }
                return@launch
            }

            _uiState.update { AccountUiState.Success(user.data) }
        }
    }
}