package zip.sora.ulearntec.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.R
import zip.sora.ulearntec.ui.screen.LoginScreen
import zip.sora.ulearntec.ui.screen.LoginUiState
import zip.sora.ulearntec.ui.screen.LoginViewModel

inline fun <reified T : Any> NavGraphBuilder.addLoginScreen(
    noinline onLoginSuccess: () -> Unit
) {
    composable<T> {
        val viewModel: LoginViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        if (uiState is LoginUiState.Success) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                (uiState as LoginUiState.Success).name?.let {
                    val text = context.getString(R.string.login_success, it)
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
                onLoginSuccess()
            }
        } else {
            LoginScreen(uiState = uiState, onSubmit = viewModel::login)
        }
    }
}