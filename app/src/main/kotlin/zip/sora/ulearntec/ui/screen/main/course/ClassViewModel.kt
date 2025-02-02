package zip.sora.ulearntec.ui.screen.main.course

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.toRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.ResourceDownload
import zip.sora.ulearntec.domain.model.state
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

    sealed interface Detail : ClassUiState {
        val live: Live

        data class Loading(
            override val clazz: Class,
            override val lives: List<Live>,
            override val live: Live
        ) : Detail

        data class Success(
            override val clazz: Class,
            override val lives: List<Live>,
            override val live: Live,
            val resources: LiveResources?,
            val download: ResourceDownload?
        ) : Detail

        data class Error(
            override val clazz: Class,
            override val lives: List<Live>,
            override val live: Live,
            val message: (Context) -> String
        ) : Detail
    }


    data class Error(
        override val clazz: Class,
        override val lives: List<Live>,
        val message: (Context) -> String
    ) : ClassUiState
}

class ClassViewModel(
    savedStateHandle: SavedStateHandle,
    private val liveRepository: LiveRepository,
    private val liveResourcesRepository: LiveResourcesRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {
    private val clazz =
        savedStateHandle.toRoute<NavGraph.Main.Course.Class>(mapOf(typeOf<Class>() to navTypeOf<Class>())).clazz

    private val _uiState = MutableStateFlow<ClassUiState>(ClassUiState.Loading(clazz, listOf()))
    val uiState = _uiState.asStateFlow()

    @OptIn(UnstableApi::class)
    fun download(context: Context) {
        val state = _uiState.value
        if (state !is ClassUiState.Detail.Success || state.resources == null) return

        when (state.download?.state) {
            null -> downloadRepository.downloadLive(context, state.resources)
            Download.STATE_COMPLETED -> downloadRepository.removeDownload(context, state.resources)
            Download.STATE_DOWNLOADING -> downloadRepository.pauseDownload(context, state.resources)
            Download.STATE_STOPPED -> downloadRepository.resumeDownload(context, state.resources)
            else -> {}
        }
    }

    private var detailJob: Job? = null

    fun showLiveDetail(live: Live) {
        if (live.resourceId == null)
            _uiState.update { ClassUiState.Detail.Success(it.clazz, it.lives, live, null, null) }
        else {
            _uiState.update { ClassUiState.Detail.Loading(it.clazz, it.lives, live) }
            detailJob = viewModelScope.launch {
                val resources = liveResourcesRepository.getLiveResources(live)
                if (resources.isError()) {
                    _uiState.update {
                        ClassUiState.Detail.Error(it.clazz, it.lives, live, resources.error)
                    }
                    return@launch
                }

                while (true) {
                    val download = downloadRepository.getDownload(resources.data).let {
                        if (it.isError()) null
                        else it.data
                    }

                    _uiState.update {
                        ClassUiState.Detail.Success(
                            it.clazz,
                            it.lives,
                            live,
                            resources.data,
                            download
                        )
                    }
                    delay(1000)
                }
            }
        }
    }

    fun hideDetail() {
        detailJob?.cancel()
        _uiState.update {
            ClassUiState.Success(it.clazz, it.lives)
        }
    }

    fun refresh(online: Boolean) {
        _uiState.update { ClassUiState.Loading(it.clazz, it.lives) }
        viewModelScope.launch {
            val lives =
                if (online) liveRepository.refresh(clazz) else liveRepository.getClassLives(clazz)
            if (lives.isError()) {
                _uiState.update { ClassUiState.Error(it.clazz, it.lives, lives.error) }
                return@launch
            }

            _uiState.update { ClassUiState.Success(clazz, lives.data) }
        }
    }

    init {
        viewModelScope.launch {
            refresh(online = false)
        }
    }
}