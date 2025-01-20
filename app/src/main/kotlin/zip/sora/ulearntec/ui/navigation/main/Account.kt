package zip.sora.ulearntec.ui.navigation.main

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.ui.screen.main.AccountScreen
import zip.sora.ulearntec.ui.screen.main.AccountViewModel


inline fun <reified T : Any> NavGraphBuilder.addAccountScreen(
    noinline onLogout: () -> Unit
) {
    composable<T> {
        val viewModel: AccountViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        AccountScreen(
            uiState = uiState,
            onRefresh = { viewModel.refresh() },
            onSettingsClicked = {},
            onLogoutClicked = {
                coroutineScope.launch {
                    viewModel.logout()
                    onLogout()
                }
            }
        )
    }
}