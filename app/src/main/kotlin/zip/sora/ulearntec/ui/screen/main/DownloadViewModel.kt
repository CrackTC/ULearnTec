package zip.sora.ulearntec.ui.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.ResourceDownload

sealed interface DownloadUiState {
    val downloads: List<ResourceDownload>
    val lives: List<Live>

    data class Loading(
        override val downloads: List<ResourceDownload>,
        override val lives: List<Live>
    ) : DownloadUiState

    data class Success(
        override val downloads: List<ResourceDownload>,
        override val lives: List<Live>
    ) : DownloadUiState

    data class Error(
        override val downloads: List<ResourceDownload>,
        override val lives: List<Live>,
        val message: (Context) -> String
    ) : DownloadUiState
}

class DownloadViewModel(
    private val downloadRepository: DownloadRepository,
    private val liveRepository: LiveRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<DownloadUiState>(
            DownloadUiState.Loading(
                downloadRepository.getAllDownloads(),
                emptyList()
            )
        )
    val uiState = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh() {
        _uiState.update { DownloadUiState.Loading(it.downloads, it.lives) }
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val downloads = downloadRepository.getAllDownloads()
            val lives = downloads.map {
                val live = liveRepository.getLive(it.resources.liveId)
                if (live is ILearnResult.Error) {
                    _uiState.update { prev ->
                        DownloadUiState.Error(prev.downloads, prev.lives, live.error!!)
                    }
                    return@launch
                }
                live.data!!
            }
            _uiState.update { DownloadUiState.Success(downloads, lives) }

            while (true) {
                val newDownloads = downloadRepository.getAllDownloads()
                val newSet = newDownloads.map { it.resources.liveId }.toSet()

                val state = _uiState.value
                val oldSet = state.downloads.map { it.resources.liveId }.toSet()

                val addedLives = (newSet - oldSet).map { liveRepository.getLive(it) }
                val error = addedLives.firstOrNull { it is ILearnResult.Error }?.error

                if (error != null) _uiState.update {
                    DownloadUiState.Error(
                        it.downloads,
                        it.lives,
                        error
                    )
                } else {
                    val idLiveMap = addedLives.associate { Pair(it.data!!.id, it.data) } +
                            state.lives.associateBy { it.id }

                    _uiState.update {
                        DownloadUiState.Success(
                            newDownloads,
                            newDownloads.map { idLiveMap[it.resources.liveId]!! })
                    }
                }

                delay(1000)
            }
        }
    }

    init {
        refresh()
    }

    fun remove(context: Context, download: ResourceDownload) {
        downloadRepository.removeDownload(context, download.resources)
    }

    fun resume(context: Context, download: ResourceDownload) {
        downloadRepository.resumeDownload(context, download.resources)
    }

    fun pause(context: Context, download: ResourceDownload) {
        downloadRepository.pauseDownload(context, download.resources)
    }
}