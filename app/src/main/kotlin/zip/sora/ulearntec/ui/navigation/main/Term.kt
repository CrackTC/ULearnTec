package zip.sora.ulearntec.ui.navigation.main

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.ui.screen.main.TermScreen
import zip.sora.ulearntec.ui.screen.main.TermUiState
import zip.sora.ulearntec.ui.screen.main.TermViewModel

inline fun <reified T : Any> NavGraphBuilder.addTermScreen(
    noinline onLoginRequired: () -> Unit,
    noinline onClassClicked: (Class) -> Unit
) {
    composable<T> {
        val viewModel: TermViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        if (uiState is TermUiState.RequireLogin) LaunchedEffect(Unit) { onLoginRequired() }
        else TermScreen(
            uiState = uiState,
            onRefresh = { viewModel.refresh() },
            onClassClicked = onClassClicked,
            onTermSelected = { viewModel.selectTerm(it) }
        )
    }
}