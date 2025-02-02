package zip.sora.ulearntec.ui.navigation.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mikepenz.aboutlibraries.entity.Library
import zip.sora.ulearntec.ui.screen.about.LicenseScreen

inline fun <reified T: Any> NavGraphBuilder.addLicenseScreen(
    noinline onBackButtonClicked: () -> Unit,
    noinline onLibraryClick: (Library) -> Unit
) {
    composable<T> {
        LicenseScreen(
            onLibraryClick = onLibraryClick,
            onBackButtonClicked = onBackButtonClicked
        )
    }
}