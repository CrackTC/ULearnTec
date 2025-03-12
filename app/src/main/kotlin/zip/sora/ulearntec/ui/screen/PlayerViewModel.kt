package zip.sora.ulearntec.ui.screen

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation.toRoute
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.PlayerCacheRepository
import zip.sora.ulearntec.domain.PlayerTheme
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.SwipeSeekMode
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveHistory
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.ResourceDownload
import zip.sora.ulearntec.domain.model.state
import zip.sora.ulearntec.playback.ClockExposedAudioRendererFactory
import zip.sora.ulearntec.playback.ClockSyncedVideoRendererFactory
import zip.sora.ulearntec.ui.navigation.NavGraph
import zip.sora.ulearntec.ui.screen.PlayerUiState.Error
import zip.sora.ulearntec.ui.screen.PlayerUiState.Loading
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.Pending
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.PlayerCreated
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.PlayerCreated.InitBuffering
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.PlayerCreated.Playing
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoading
import java.time.Instant

data class GesturePreferences(
    val swipeSeekMode: SwipeSeekMode,
    val swipeSeekFixedMillis: Long,
    val swipeSeekPercent: Float,
    val swipeVolumePercent: Float,
    val swipeBrightnessPercent: Float,
    val longPressSpeed: Float
)

sealed interface PlayerUiState {
    val live: Live?

    data class Loading(
        override val live: Live?,
    ) : PlayerUiState

    sealed interface PreferenceLoaded : PlayerUiState {
        val theme: PlayerTheme
        val gesturePreferences: GesturePreferences

        data class ResourceLoading(
            override val live: Live,
            override val theme: PlayerTheme,
            override val gesturePreferences: GesturePreferences
        ) : PreferenceLoaded

        sealed interface ResourceLoaded : PreferenceLoaded {
            val liveResources: LiveResources
            val download: ResourceDownload?

            data class Pending(
                override val liveResources: LiveResources,
                override val download: ResourceDownload?,
                override val theme: PlayerTheme,
                override val gesturePreferences: GesturePreferences,
                override val live: Live,
            ) : ResourceLoaded

            sealed interface PlayerCreated : ResourceLoaded {
                val videoPlayers: List<Player>
                val audioPlayer: Player
                val requestedSpeed: Float

                data class InitBuffering(
                    override val liveResources: LiveResources,
                    override val download: ResourceDownload?,
                    override val theme: PlayerTheme,
                    override val gesturePreferences: GesturePreferences,
                    override val videoPlayers: List<Player>,
                    override val audioPlayer: Player,
                    override val requestedSpeed: Float,
                    override val live: Live,
                ) : PlayerCreated

                data class Playing(
                    override val liveResources: LiveResources,
                    override val download: ResourceDownload?,
                    override val theme: PlayerTheme,
                    override val gesturePreferences: GesturePreferences,
                    override val videoPlayers: List<Player>,
                    val aspectRatios: List<Float>,
                    override val audioPlayer: Player,
                    override val requestedSpeed: Float,
                    val isPlaying: Boolean,
                    val cues: ImmutableList<Cue>,
                    val currentMillis: Long,
                    val totalMillis: Long,
                    override val live: Live,
                ) : PlayerCreated
            }
        }
    }

    data class Error(
        val message: (Context) -> String,
        override val live: Live?,
    ) : PlayerUiState
}

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val downloadDataSourceFactory: DataSource.Factory,
    private val liveRepository: LiveRepository,
    private val liveResourcesRepository: LiveResourcesRepository,
    private val downloadRepository: DownloadRepository,
    private val playerCacheRepository: PlayerCacheRepository,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val liveId =
        savedStateHandle.toRoute<NavGraph.Player>().liveId
    private val _uiState =
        MutableStateFlow<PlayerUiState>(Loading(null))
    val uiState = _uiState.asStateFlow()

    init {
        initializeResources()
        viewModelScope.launch { updateMillis() }
    }

    fun setSpeed(speed: Float) {
        val state = _uiState.value
        if (state !is PlayerCreated) return

        state.audioPlayer.setPlaybackSpeed(speed)
        state.videoPlayers.forEach { it.setPlaybackSpeed(speed) }
        _uiState.update { prevState ->
            when (prevState) {
                is Playing ->
                    prevState.copy(requestedSpeed = speed)

                is InitBuffering ->
                    prevState.copy(requestedSpeed = speed)

                else -> prevState
            }
        }
    }

    fun play() {
        val state = _uiState.value
        if (state !is Playing) return

        _uiState.update { state.copy(isPlaying = true) }

        state.audioPlayer.play()
        state.videoPlayers.forEach { it.play() }
    }

    fun pause() {
        val state = _uiState.value
        if (state !is Playing) return

        _uiState.update { state.copy(isPlaying = false) }

        state.audioPlayer.pause()
        state.videoPlayers.forEach { it.pause() }
    }

    fun retry() {
        val state = _uiState.value
        if (state !is Error) throw IllegalStateException()

        _uiState.update { Loading(null) }
        initializeResources()
    }

    fun rewind() {
        val state = _uiState.value
        if (state !is PlayerCreated) return

        state.audioPlayer.seekBack()
        state.videoPlayers.forEach { it.seekBack() }
    }

    fun forward() {
        val state = _uiState.value
        if (state !is PlayerCreated) return

        state.audioPlayer.seekForward()
        state.videoPlayers.forEach { it.seekForward() }
    }

    fun seek(millis: Long) {
        val state = _uiState.value
        if (state !is Playing) return

        state.audioPlayer.seekTo(millis)
        state.videoPlayers.forEach { it.seekTo(state.audioPlayer.currentPosition) }
        _uiState.update { state.copy(currentMillis = millis) }
    }

    private fun initializeResources() {
        viewModelScope.launch {
            val theme = preferenceRepository.getPlayerTheme()
            val gesturePreferences = preferenceRepository.let {
                GesturePreferences(
                    it.getSwipeSeekMode(),
                    it.getSwipeSeekFixedMillis(),
                    it.getSwipeSeekPercent(),
                    it.getSwipeVolumePercent(),
                    it.getSwipeBrightnessPercent(),
                    it.getLongPressSpeed()
                )
            }

            val live = liveRepository.getLive(liveId)
            if (live.isError()) {
                _uiState.update { Error(live.error, null) }
                return@launch
            }

            _uiState.update {
                ResourceLoading(
                    live.data,
                    theme,
                    gesturePreferences
                )
            }

            val resources = liveResourcesRepository.getLiveResources(live.data)
            if (resources.isError()) {
                _uiState.update { Error(resources.error, it.live) }
                return@launch
            }

            val download = downloadRepository.getDownload(resources.data)
            if (download.isError()) {
                // no download found, pass null
                _uiState.update {
                    it as ResourceLoading
                    Pending(
                        resources.data,
                        null,
                        it.theme,
                        it.gesturePreferences,
                        it.live
                    )
                }
                return@launch
            }
            _uiState.update {
                it as ResourceLoading
                Pending(
                    resources.data,
                    download.data,
                    it.theme,
                    it.gesturePreferences,
                    it.live
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun initializePlayers(context: Context) {
        val state = _uiState.value
        if (state !is Pending) return

        val dataSourceFactory =
            if (state.download?.state == Download.STATE_COMPLETED) downloadDataSourceFactory
            else playerCacheRepository.getCacheFactory(context)

        var mediaClockPosition = 0L
        val audioPlayer =
            (state.liveResources.audioPath.ifBlank { state.liveResources.videoList[0].videoPath }).let {
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(
                        DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
                    )
                    .setRenderersFactory(ClockExposedAudioRendererFactory(context) {
                        mediaClockPosition = it
                    })
                    .build()
                    .apply {
                        trackSelectionParameters = trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
                            .build()
                        val builder = MediaItem.Builder().setUri(it)
                        val phaseUrl = state.liveResources.phaseUrl
                        if (phaseUrl.isNotBlank()) {
                            builder.setSubtitleConfigurations(
                                listOf(
                                    MediaItem.SubtitleConfiguration.Builder(phaseUrl.toUri())
                                        .setMimeType(MimeTypes.TEXT_VTT)
                                        .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                                        .build()
                                )
                            )
                        }
                        setMediaItem(builder.build())
                    }
            }

        val players = state.liveResources.videoList.map {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
                )
                .setRenderersFactory(ClockSyncedVideoRendererFactory(context) { mediaClockPosition })
                .build()
                .apply {
                    trackSelectionParameters = trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                        .build()
                    setMediaItem(MediaItem.fromUri(it.videoPath))
                }
        }

        _uiState.update {
            InitBuffering(
                state.liveResources,
                state.download,
                state.theme,
                state.gesturePreferences,
                players,
                audioPlayer,
                1.0f,
                state.live
            )
        }

        audioPlayer.apply {
            addListener(getListener(null))
            prepare()
            seekTo(state.live.history?.positionMillis ?: 0L)
        }

        players.forEachIndexed { index, player ->
            player.apply {
                addListener(getListener(index))
                prepare()
                seekTo(state.live.history?.positionMillis ?: 0L)
            }
        }
    }

    private suspend fun updateMillis() {
        while (true) {
            val state = _uiState.first {
                it is Playing && it.isPlaying
            } as Playing

            var currentPosition = state.audioPlayer.currentPosition
            var delayMillis = 1000 - (currentPosition % 1000)
            if (delayMillis < 20) {
                currentPosition += delayMillis
                delayMillis += 1000
            }

            liveRepository.updateHistory(
                LiveHistory(
                    state.live.id,
                    Instant.now().toEpochMilli(),
                    currentPosition
                )
            )

            _uiState.update {
                state.copy(
                    currentMillis = currentPosition,
                    totalMillis = state.audioPlayer.duration.let { if (it > 0) it else currentPosition }
                )
            }

            val speed = state.audioPlayer.playbackParameters.speed
            delayMillis = (delayMillis / speed).toLong()

            delay(delayMillis)
        }
    }

    var readyCount = 0
    private fun getListener(playerIndex: Int?) = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            _uiState.update { state ->
                (state as? PlayerCreated)?.apply {
                    videoPlayers.forEach { it.release() }
                    audioPlayer.release()
                }
                Error({ error.message ?: error.errorCodeName }, state.live)
            }
        }

        override fun onCues(cueGroup: CueGroup) {
            _uiState.update { prevState ->
                if (prevState !is Playing) prevState
                else prevState.copy(cues = cueGroup.cues)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (playerIndex == null) return
            _uiState.update { prevState ->
                when (prevState) {
                    is Playing -> {
                        prevState.copy(
                            aspectRatios = prevState.aspectRatios.mapIndexed { index, ratio ->
                                if (index != playerIndex) ratio
                                else videoSize.width.toFloat() / videoSize.height * videoSize.pixelWidthHeightRatio
                            }
                        )
                    }

                    else -> prevState
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState != Player.STATE_READY) return

            val count = ++readyCount
            _uiState.update { prev ->
                if (prev is InitBuffering && count > prev.videoPlayers.size) {
                    prev.audioPlayer.play()
                    prev.videoPlayers.forEach { it.play() }
                    Playing(
                        prev.liveResources,
                        prev.download,
                        prev.theme,
                        prev.gesturePreferences,
                        prev.videoPlayers,
                        prev.videoPlayers.map {
                            it.videoSize.let { size ->
                                if (size.height > 0)
                                    size.width.toFloat() / size.height * size.pixelWidthHeightRatio
                                else 16.0f / 9.0f
                            }
                        },
                        prev.audioPlayer,
                        prev.requestedSpeed,
                        true,
                        ImmutableList.of(),
                        prev.audioPlayer.currentPosition,
                        prev.audioPlayer.duration.let { if (it > 0) it else prev.audioPlayer.currentPosition },
                        prev.live
                    )
                } else prev
            }
        }
    }

    override fun onCleared() {
        (_uiState.value as? PlayerCreated)?.apply {
            videoPlayers.forEach { it.release() }
            audioPlayer.release()
        }
    }
}