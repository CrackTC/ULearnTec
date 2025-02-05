package zip.sora.ulearntec.ui.navigation

import android.media.AudioManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.window.layout.WindowMetricsCalculator
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.PlayerTheme
import zip.sora.ulearntec.ui.ForceLandscape
import zip.sora.ulearntec.ui.HideSystemBars
import zip.sora.ulearntec.ui.OverrideBrightness
import zip.sora.ulearntec.ui.screen.PlayerScreen
import zip.sora.ulearntec.ui.screen.PlayerUiState
import zip.sora.ulearntec.ui.screen.PlayerViewModel
import zip.sora.ulearntec.ui.theme.Typography
import zip.sora.ulearntec.ui.theme.getColorScheme

@Composable
@Stable
fun playerColorScheme(theme: PlayerTheme?): ColorScheme {
    return when (theme) {
        PlayerTheme.FOLLOW_THEME -> MaterialTheme.colorScheme
        PlayerTheme.SYSTEM -> getColorScheme(isSystemInDarkTheme(), true)
        PlayerTheme.LIGHT -> getColorScheme(darkTheme = false, true)
        PlayerTheme.DARK -> getColorScheme(darkTheme = true, true)
        else -> MaterialTheme.colorScheme
    }
}

inline fun <reified T : Any> NavGraphBuilder.addPlayerScreen(
    noinline onBackButtonClicked: () -> Unit
) {
    composable<T> {
        val context = LocalContext.current
        val windowMetrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(LocalContext.current)

        val viewModel: PlayerViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        ForceLandscape()
        HideSystemBars()

        val resourceLoaded by remember {
            derivedStateOf { uiState is PlayerUiState.PreferenceLoaded.ResourceLoaded }
        }

        if (resourceLoaded) {
            LaunchedEffect(Unit) { viewModel.initializePlayers(context) }
        }

        val theme by remember {
            derivedStateOf { (uiState as? PlayerUiState.PreferenceLoaded)?.theme }
        }

        MaterialTheme(colorScheme = playerColorScheme(theme), typography = Typography) {

            // seems that there's no elegant way to get current brightness,
            // simply use hardcoded initial value here
            var brightness by remember { mutableFloatStateOf(0.5f) }
            var overrideBrightness by remember { mutableStateOf(false) }
            if (overrideBrightness) OverrideBrightness(brightness)


            val audioManager: AudioManager = remember { context.getSystemService()!! }
            var volumePercent by remember {
                mutableFloatStateOf(
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                )
            }

            PlayerScreen(
                uiState = uiState,
                windowMetrics = windowMetrics,
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
}