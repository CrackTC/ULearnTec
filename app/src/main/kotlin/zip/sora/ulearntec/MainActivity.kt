package zip.sora.ulearntec

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.Theme
import zip.sora.ulearntec.ui.navigation.NavGraph
import zip.sora.ulearntec.ui.theme.ULearnTecTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // https://gan0803.dev/blog/2022-01-27-fully-transparent-system-bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.setNavigationBarContrastEnforced(false)

        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val theme = (uiState as? MainUiState.Success)?.theme ?: return@setContent

            ULearnTecTheme(
                darkTheme = when (theme) {
                    Theme.SYSTEM -> isSystemInDarkTheme()
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                }
            ) {
                // add background to avoid white flicker on NavHost initialization
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(onThemeChanged = {
                        viewModel.setTheme(it)
                    })
                }
            }
        }
    }
}