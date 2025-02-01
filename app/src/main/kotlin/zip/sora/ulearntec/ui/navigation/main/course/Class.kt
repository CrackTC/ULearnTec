package zip.sora.ulearntec.ui.navigation.main.course

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.navigation.LocalNavAnimatedVisibilityScope
import zip.sora.ulearntec.ui.navigation.navTypeOf
import zip.sora.ulearntec.ui.screen.main.course.ClassScreen
import zip.sora.ulearntec.ui.screen.main.course.ClassViewModel
import kotlin.reflect.typeOf

inline fun <reified T : Any> NavGraphBuilder.addClassScreen(
    noinline onBackButtonClicked: () -> Unit,
    noinline onLiveClicked: (Live) -> Unit,
) {
    composable<T>(typeMap = mapOf(typeOf<Class>() to navTypeOf<Class>())) {
        val context = LocalContext.current
        val viewModel: ClassViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()

        var wentToPlay by rememberSaveable { mutableStateOf(false) }

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            if (wentToPlay) {
                wentToPlay = false
                viewModel.refresh(online = false)
            }
        }

        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            ClassScreen(
                uiState = uiState,
                onRefresh = { viewModel.refresh(online = true) },
                onBackButtonClicked = onBackButtonClicked,
                onLiveClicked = {
                    wentToPlay = true
                    onLiveClicked(it)
                },
                onShowLiveDetail = { viewModel.showLiveDetail(it) },
                onHideLiveDetail = { viewModel.hideDetail() },
                onDownloadClicked = { viewModel.download(context) }
            )
        }
    }
}