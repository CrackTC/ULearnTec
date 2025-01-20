package zip.sora.ulearntec.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import zip.sora.ulearntec.R

data class NavBarItemData<T : Any>(
    val route: T,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val mainNavBarItems = listOf(
    NavBarItemData(
        route = NavGraph.Main.Course,
        label = R.string.course,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    ),
    NavBarItemData(
        route = NavGraph.Main.History,
        label = R.string.history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    NavBarItemData(
        route = NavGraph.Main.Account,
        label = R.string.account,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)