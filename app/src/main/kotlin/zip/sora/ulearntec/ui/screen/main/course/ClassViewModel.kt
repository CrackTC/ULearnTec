package zip.sora.ulearntec.ui.screen.main.course

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.navigation.NavGraph
import zip.sora.ulearntec.ui.navigation.navTypeOf
import kotlin.reflect.typeOf

sealed interface ClassUiState {
    val clazz: Class
    val lives: List<Live>

    data class Loading(
        override val clazz: Class,
        override val lives: List<Live>
    ) : ClassUiState

    data class Success(
        override val clazz: Class,
        override val lives: List<Live>
    ) : ClassUiState

    data class Error(
        override val clazz: Class,
        override val lives: List<Live>,
        val message: (Context) -> String
    ) : ClassUiState
}

class ClassViewModel(
    savedStateHandle: SavedStateHandle,
    private val liveRepository: LiveRepository
) : ViewModel() {
    private val clazz =
        savedStateHandle.toRoute<NavGraph.Main.Course.Class>(mapOf(typeOf<Class>() to navTypeOf<Class>())).clazz

    private val _uiState = MutableStateFlow<ClassUiState>(ClassUiState.Loading(clazz, listOf()))
    val uiState = _uiState.asStateFlow()

    fun refresh(online: Boolean) {
        _uiState.update { ClassUiState.Loading(it.clazz, it.lives) }
        viewModelScope.launch {
            val lives = if (online) liveRepository.refresh() else liveRepository.getClassLives()
            if (lives is ILearnResult.Error) {
                _uiState.update { ClassUiState.Error(it.clazz, it.lives, lives.error!!) }
                return@launch
            }

            _uiState.update { ClassUiState.Success(clazz, lives.data!!) }
        }
    }

    init {
        viewModelScope.launch {
            liveRepository.setCurrentClass(clazz)
            refresh(online = false)
        }
    }
}