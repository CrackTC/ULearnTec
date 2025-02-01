package zip.sora.ulearntec.ui.navigation.main

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.screen.main.HistoryScreen
import zip.sora.ulearntec.ui.screen.main.HistoryViewModel

inline fun <reified T : Any> NavGraphBuilder.addHistoryScreen(
    noinline onLiveClicked: (Live) -> Unit,
    noinline onGotoClass: (Class) -> Unit
) {
    composable<T> {
        val viewModel: HistoryViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

        HistoryScreen(
            uiState = uiState,
            onLiveClicked = onLiveClicked,
            onRemove = viewModel::remove,
            onRefresh = viewModel::refresh,
            // ugly:(, TODO: refactor ClassScreen, pass a placeholder Class and refresh it?
            onGotoClass = {
                coroutineScope.launch { viewModel.fetchClass(it)?.let(onGotoClass) }
            }
        )
    }
}