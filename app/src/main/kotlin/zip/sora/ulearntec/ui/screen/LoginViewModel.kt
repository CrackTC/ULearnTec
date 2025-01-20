package zip.sora.ulearntec.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.UserRepository

sealed interface LoginUiState {
    data object Normal : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: (Context) -> String) : LoginUiState
    data class Success(val name: String?) : LoginUiState
}

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Normal)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (userRepository.isLoggedIn()) {
                _uiState.update { LoginUiState.Success(null) }
            }
        }
    }

    fun login(username: String, password: String) {
        _uiState.update { LoginUiState.Loading }

        viewModelScope.launch {
            try {
                when (val res = userRepository.login(username, password)) {
                    is ILearnResult.Success -> _uiState.update {
                        LoginUiState.Success(res.data!!.studentName)
                    }

                    else -> _uiState.update { LoginUiState.Error(res.error!!) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { LoginUiState.Error({ e.stackTraceToString() }) }
            }
        }
    }
}