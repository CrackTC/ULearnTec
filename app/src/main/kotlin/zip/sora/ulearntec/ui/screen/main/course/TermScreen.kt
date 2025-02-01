package zip.sora.ulearntec.ui.screen.main.course

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.ClassStatus
import zip.sora.ulearntec.domain.model.Term
import zip.sora.ulearntec.ui.component.ClassCardHorizontal
import zip.sora.ulearntec.ui.component.ClassCardVertical
import zip.sora.ulearntec.ui.component.DetailEntry
import zip.sora.ulearntec.ui.component.DetailSheet
import zip.sora.ulearntec.ui.component.ErrorPane
import zip.sora.ulearntec.ui.mockClasses
import zip.sora.ulearntec.ui.mockTerms
import zip.sora.ulearntec.ui.navigation.LocalNavAnimatedVisibilityScope
import zip.sora.ulearntec.ui.navigation.LocalSharedTransitionScope
import zip.sora.ulearntec.ui.theme.ULearnTecTheme
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermScreen(
    uiState: TermUiState,
    onRefresh: () -> Unit,
    onTermSelected: (Term) -> Unit,
    onClassClicked: (Class) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedStatus by rememberSaveable { mutableStateOf(ClassStatus.NONE) }
    var gridMode by rememberSaveable { mutableStateOf(false) }
    var showTermDetail by rememberSaveable { mutableStateOf(false) }

    if (showTermDetail) {
        val term = uiState.currentTerm
        if (term == null) {
            showTermDetail = false
        } else {
            DetailSheet(
                title = R.string.term_detail,
                onDismissRequest = { showTermDetail = false },
                entries = listOf(
                    DetailEntry(
                        name = R.string.year,
                        value = term.year.toString(),
                        icon = Icons.Filled.CalendarToday,
                    ),
                    DetailEntry(
                        name = R.string.semester,
                        value = term.num.toString(),
                        icon = Icons.AutoMirrored.Filled.ListAlt
                    ),
                    DetailEntry(
                        name = R.string.term_name,
                        value = term.name,
                        icon = Icons.Filled.LocalOffer
                    ),
                    DetailEntry(
                        name = R.string.term_id,
                        value = term.id,
                        icon = Icons.AutoMirrored.Filled.LibraryBooks
                    ),
                    DetailEntry(
                        name = R.string.term_start_date,
                        value = term.startDate,
                        icon = Icons.Filled.FlightTakeoff
                    ),
                    DetailEntry(
                        name = R.string.term_end_date,
                        value = term.endDate,
                        icon = Icons.Filled.FlightLand
                    ),
                    DetailEntry(
                        name = R.string.last_updated,
                        value = DateUtils.getRelativeTimeSpanString(
                            term.lastUpdated,
                            Instant.now().toEpochMilli(),
                            0
                        ).toString(),
                        icon = Icons.Filled.History
                    )
                ),
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.classes)) }) },
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = uiState is TermUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {

                SelectionsChipRow(
                    enabled = uiState !is TermUiState.Loading,

                    selectedTerm = uiState.currentTerm,
                    terms = uiState.terms,
                    onTermSelected = onTermSelected,

                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = it },

                    gridMode = gridMode,
                    onGridModeChanged = { gridMode = it },

                    onShowTermDetailClicked = { showTermDetail = true },

                    modifier = Modifier.fillMaxWidth()
                )

                val filteredClasses = uiState.classes.filter {
                    (it.status == selectedStatus || selectedStatus == ClassStatus.NONE)
                }

                AnimatedContent(gridMode) { gridMode ->
                    if (gridMode) {
                        ClassGrid(
                            classes = filteredClasses,
                            onClassClicked = onClassClicked,
                            screenAnimatedVisibilityScope = this,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        ClassList(
                            classes = filteredClasses,
                            onClassClicked = onClassClicked,
                            screenAnimatedVisibilityScope = this,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                if (uiState is TermUiState.Error) {
                    ErrorPane(
                        uiState.message(LocalContext.current),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ClassList(
    classes: List<Class>,
    onClassClicked: (Class) -> Unit,
    screenAnimatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current!!
    with(LocalSharedTransitionScope.current!!) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(items = classes, key = { it.id }) { clazz ->
                ClassCardHorizontal(
                    clazz = clazz,
                    onClick = { onClassClicked(clazz) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .animateItem()
                        .sharedElement(
                            rememberSharedContentState("list-${clazz.id}"),
                            navAnimatedVisibilityScope
                        )
                        .sharedElement(
                            rememberSharedContentState("class-item-${clazz.id}"),
                            screenAnimatedVisibilityScope
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ClassGrid(
    classes: List<Class>,
    onClassClicked: (Class) -> Unit,
    screenAnimatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current!!
    with(LocalSharedTransitionScope.current!!) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = modifier
        ) {
            items(items = classes, key = { it.id }) { clazz ->
                ClassCardVertical(
                    clazz = clazz,
                    onClick = { onClassClicked(clazz) },
                    modifier = Modifier
                        .animateItem()
                        .sharedElement(
                            rememberSharedContentState(key = "grid-${clazz.id}"),
                            navAnimatedVisibilityScope
                        )
                        .sharedElement(
                            rememberSharedContentState(key = "class-item-${clazz.id}"),
                            screenAnimatedVisibilityScope
                        )
                )
            }
        }
    }
}

@Composable
private fun SelectionsChipRow(
    enabled: Boolean,

    selectedTerm: Term?,
    terms: List<Term>,
    onTermSelected: (Term) -> Unit,

    selectedStatus: ClassStatus,
    onStatusSelected: (ClassStatus) -> Unit,

    gridMode: Boolean,
    onGridModeChanged: (Boolean) -> Unit,

    onShowTermDetailClicked: () -> Unit,

    modifier: Modifier = Modifier
) {
    var termSelectorExpanded by rememberSaveable { mutableStateOf(false) }
    var statusSelectorExpanded by rememberSaveable { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.padding(4.dp))
        Box {
            AssistChip(
                enabled = enabled,
                onClick = { termSelectorExpanded = true },
                label = {
                    AnimatedContent(selectedTerm) {
                        Text(text = it?.let { "${it.year} ${it.name}" }
                            ?: stringResource(R.string.loading))
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
            TermsDropdownMenu(
                terms = terms,
                expanded = termSelectorExpanded,
                onDismissRequest = { termSelectorExpanded = false },
                onTermSelected = {
                    termSelectorExpanded = false
                    onTermSelected(it)
                }
            )
        }
        Box {
            AssistChip(
                enabled = enabled,
                onClick = { statusSelectorExpanded = true },
                label = {
                    AnimatedContent(selectedStatus) { Text(text = stringResource(it.description)) }
                },
                leadingIcon = {
                    AnimatedContent(selectedStatus) {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                }
            )
            StatusDropdownMenu(
                expanded = statusSelectorExpanded,
                onDismissRequest = { statusSelectorExpanded = false },
                onStatusSelected = {
                    statusSelectorExpanded = false
                    onStatusSelected(it)
                }
            )
        }
        FilterChip(
            enabled = enabled,
            selected = gridMode,
            onClick = { onGridModeChanged(!gridMode) },
            label = { Text(stringResource(R.string.grid)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )
        AssistChip(
            enabled = enabled,
            onClick = onShowTermDetailClicked,
            label = {
                Text(text = stringResource(R.string.term_info))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        Spacer(modifier = Modifier.padding(4.dp))
    }
}

@Composable
private fun StatusDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onStatusSelected: (ClassStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        ClassStatus.entries.forEach {
            DropdownMenuItem(
                text = { Text(text = stringResource(it.description)) },
                onClick = { onStatusSelected(it) }
            )
        }
    }
}

@Composable
private fun TermsDropdownMenu(
    terms: List<Term>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onTermSelected: (Term) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        terms.forEach { term ->
            DropdownMenuItem(
                text = { Text(text = "${term.year} ${term.name}") },
                onClick = { onTermSelected(term) })
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun TermScreenPreview() {
    ULearnTecTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility
                ) {
                    TermScreen(
                        onRefresh = {},
                        onClassClicked = {},
                        onTermSelected = {},
                        uiState = TermUiState.Success(
                            currentTerm = mockTerms[0],
                            terms = mockTerms,
                            classes = mockClasses
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun TermScreenErrorPreview() {
    ULearnTecTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility
                ) {
                    TermScreen(
                        uiState = TermUiState.Error(
                            { "Human is dead, mismatch" },
                            currentTerm = null,
                            emptyList(),
                            emptyList()
                        ),
                        onRefresh = {},
                        onTermSelected = {},
                        onClassClicked = {}
                    )
                }
            }
        }
    }
}