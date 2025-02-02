package zip.sora.ulearntec.ui.navigation.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import zip.sora.ulearntec.ui.screen.about.AboutScreen

inline fun <reified T : Any> NavGraphBuilder.addAboutScreen(
    noinline onBackButtonClicked: () -> Unit,
    noinline onLicenseClicked: () -> Unit
) {
    composable<T> {
        AboutScreen(
            onBackButtonClicked = onBackButtonClicked,
            onLicenseClicked = onLicenseClicked
        )
    }
}