package zip.sora.ulearntec.ui.navigation

import android.media.AudioManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.ForceLandscape
import zip.sora.ulearntec.ui.HideSystemBars
import zip.sora.ulearntec.ui.OverrideBrightness
import zip.sora.ulearntec.ui.screen.PlayerScreen
import zip.sora.ulearntec.ui.screen.PlayerUiState
import zip.sora.ulearntec.ui.screen.PlayerViewModel
import kotlin.reflect.typeOf

inline fun <reified T : Any> NavGraphBuilder.addPlayerScreen(
    noinline onBackButtonClicked: () -> Unit
) {
    composable<T> {
        val context = LocalContext.current

        // seems that there's no elegant way to get current brightness,
        // simply use hardcoded initial value here
        var brightness by remember { mutableFloatStateOf(0.5f) }
        var overrideBrightness by remember { mutableStateOf(false) }

        val audioManager: AudioManager = remember { context.getSystemService()!! }
        var volumePercent by remember {
            mutableFloatStateOf(
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            )
        }

        if (overrideBrightness) {
            OverrideBrightness(brightness)
        }
        ForceLandscape()
        HideSystemBars()

        val viewModel: PlayerViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        if (uiState is PlayerUiState.ResourceKnown.Pending) {
            LaunchedEffect(Unit) { viewModel.initializePlayers(context) }
        }

        PlayerScreen(
            uiState = uiState,
            onBackButtonClicked = onBackButtonClicked,
            onPlay = viewModel::play,
            onPause = viewModel::pause,
            onRewind = viewModel::rewind,
            onForward = viewModel::forward,
            onSeek = viewModel::seek,
            onRetry = viewModel::retry,
            onSpeed = viewModel::setSpeed,
            currentVolumePercent = volumePercent,
            currentBrightnessPercent = brightness,
            onVolumeDelta = { delta ->
                volumePercent = (volumePercent + delta).coerceIn(0.0f, 1.0f)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val newVolume = maxVolume * volumePercent
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.toInt(), 0)
            },
            onBrightnessDelta = {
                overrideBrightness = true
                brightness = (brightness + it).coerceIn(0.0f, 1.0f)
            }
        )
    }
}