package zip.sora.ulearntec.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.Theme
import zip.sora.ulearntec.ui.screen.SettingsScreen
import zip.sora.ulearntec.ui.screen.SettingsViewModel

inline fun <reified T : Any> NavGraphBuilder.addSettingsScreen(
    noinline onThemeChanged: (Theme) -> Unit,
    noinline onBackButtonClicked: () -> Unit
) {
    composable<T> {
        val viewModel: SettingsViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        SettingsScreen(
            uiState,
            onDataExpireMillisChanged = {
                viewModel.setDataExpireMillis(it)
            },
            onThemeChanged = {
                viewModel.setTheme(it)
                onThemeChanged(it)
            },
            onPlayerThemeChanged = {
                viewModel.setPlayerTheme(it)
            },
            onMaxPlayerCacheMbChanged = {
                viewModel.setMaxPlayerCacheMb(it)
            },
            onSwipeSeekModeChanged = {
                viewModel.setSwipeSeekMode(it)
            },
            onSwipeSeekFixedMillisChanged = {
                viewModel.setSwipeSeekFixedMillis(it)
            },
            onSwipeSeekPercentChanged = {
                viewModel.setSwipeSeekPercent(it)
            },
            onSwipeVolumePercentChanged = {
                viewModel.setSwipeVolumePercent(it)
            },
            onSwipeBrightnessPercentChanged = {
                viewModel.setSwipeBrightnessPercent(it)
            },
            onLongPressSpeedChanged = {
                viewModel.setLongPressSpeed(it)
            },
            onBackButtonClicked = onBackButtonClicked
        )
    }
}