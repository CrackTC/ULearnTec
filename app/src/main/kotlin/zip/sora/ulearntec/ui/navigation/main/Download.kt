package zip.sora.ulearntec.ui.navigation.main

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.screen.main.DownloadScreen
import zip.sora.ulearntec.ui.screen.main.DownloadViewModel

inline fun <reified T: Any> NavGraphBuilder.addDownloadScreen(
    noinline onWatch: (Live) -> Unit
) {
    composable<T> {
        val context = LocalContext.current
        val viewModel: DownloadViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        DownloadScreen(
            uiState = uiState,
            onResume = {viewModel.resume(context, it)},
            onPause = {viewModel.pause(context, it)},
            onRemove = {viewModel.remove(context, it)},
            onLongClick = {},
            onWatch = onWatch
        )
    }
}