package zip.sora.ulearntec.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import zip.sora.ulearntec.ui.navigation.main.addAccountScreen
import zip.sora.ulearntec.ui.navigation.main.addHistoryScreen
import zip.sora.ulearntec.ui.navigation.main.course.addClassScreen
import zip.sora.ulearntec.ui.navigation.main.course.addTermScreen
import zip.sora.ulearntec.domain.model.Class as ClassModel

@Serializable
object NavGraph {
    @Serializable
    object Login

    @Serializable
    object Main {
        @Serializable
        object Course {
            @Serializable
            object Term

            @Serializable
            data class Class(val clazz: ClassModel)
        }

        @Serializable
        object History

        @Serializable
        object Account
    }

    @Serializable
    data class Player(val liveId: String)
}

@Composable
fun MainNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        mainNavBarItems.forEach { item ->
            val selected =
                currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
            NavigationBarItem(
                alwaysShowLabel = false,
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(NavGraph.Main.Course.Term) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(text = stringResource(item.label)) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.label)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavGraph.Main,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        route = NavGraph::class,
        modifier = Modifier.fillMaxSize()
    ) {
        addLoginScreen<NavGraph.Login>(
            onLoginSuccess = { navController.navigate(NavGraph.Main) { popUpTo(NavGraph) } }
        )
        addPlayerScreen<NavGraph.Player>(
            onBackButtonClicked = navController::popBackStack
        )
        composable<NavGraph.Main> {
            val mainNavController = rememberNavController()
            Scaffold(bottomBar = { MainNavBar(mainNavController) }) { innerPadding ->
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(
                            navController = mainNavController,
                            startDestination = NavGraph.Main.Course,
                            route = NavGraph.Main::class,
                            enterTransition = { fadeIn(animationSpec = tween(250)) },
                            exitTransition = { fadeOut(animationSpec = tween(250)) },
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            navigation<NavGraph.Main.Course>(startDestination = NavGraph.Main.Course.Term) {
                                addTermScreen<NavGraph.Main.Course.Term>(
                                    onLoginRequired = {
                                        navController.navigate(NavGraph.Login) { popUpTo(NavGraph) }
                                    },
                                    onClassClicked = {
                                        mainNavController.navigate(NavGraph.Main.Course.Class(it))
                                    }
                                )
                                addClassScreen<NavGraph.Main.Course.Class>(
                                    onBackButtonClicked = mainNavController::popBackStack,
                                    onLiveClicked = {
                                        navController.navigate(NavGraph.Player(it.id))
                                    }
                                )
                            }
                            addHistoryScreen<NavGraph.Main.History>(
                                onLiveClicked = {
                                    navController.navigate(NavGraph.Player(it.id))
                                },
                                onGotoClass = {
                                    mainNavController.navigate(NavGraph.Main.Course.Class(it))
                                }
                            )
                            addAccountScreen<NavGraph.Main.Account>(
                                onLogout = {
                                    navController.navigate(NavGraph.Login) { popUpTo(NavGraph) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
