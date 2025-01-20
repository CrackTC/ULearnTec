package zip.sora.ulearntec

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
            ULearnTecTheme {
                // add background to avoid white flicker on NavHost initialization
                Surface(modifier = Modifier.fillMaxSize()) { NavGraph() }
            }
        }
    }
}