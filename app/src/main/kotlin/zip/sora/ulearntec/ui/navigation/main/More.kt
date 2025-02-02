package zip.sora.ulearntec.ui.navigation.main

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.ui.screen.main.MoreScreen
import zip.sora.ulearntec.ui.screen.main.MoreViewModel


inline fun <reified T : Any> NavGraphBuilder.addMoreScreen(
    noinline onLogout: () -> Unit,
    noinline onAboutClicked: () -> Unit,
    noinline onSettingsClicked: () -> Unit,
) {
    composable<T> {
        val viewModel: MoreViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        MoreScreen(
            uiState = uiState,
            onRefresh = { viewModel.refresh() },
            onSettingsClicked = onSettingsClicked,
            onAboutClicked = onAboutClicked,
            onLogoutClicked = {
                coroutineScope.launch {
                    viewModel.logout()
                    onLogout()
                }
            }
        )
    }
}