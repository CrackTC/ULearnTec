package zip.sora.ulearntec.ui.navigation.about

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import zip.sora.ulearntec.ui.screen.about.LicenseDetailScreen

inline fun <reified T : Any> NavGraphBuilder.addLicenseDetailScreen(
    noinline onBackButtonClicked: () -> Unit,
    noinline nameSelector: (T) -> String,
    noinline websiteSelector: (T) -> String?,
    noinline licenseSelector: (T) -> String
) {
    composable<T> {
        val argument = it.toRoute<T>()
        LicenseDetailScreen(
            name = nameSelector(argument),
            website = websiteSelector(argument),
            license = licenseSelector(argument),
            onBackButtonClicked = onBackButtonClicked
        )
    }
}