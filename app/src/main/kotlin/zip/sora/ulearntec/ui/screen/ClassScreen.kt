package zip.sora.ulearntec.ui.screen

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Room
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import coil3.compose.AsyncImage
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveStatus
import zip.sora.ulearntec.domain.model.progress
import zip.sora.ulearntec.domain.model.state
import zip.sora.ulearntec.ui.component.ClassCardHorizontal
import zip.sora.ulearntec.ui.component.DetailEntry
import zip.sora.ulearntec.ui.component.DetailSheet
import zip.sora.ulearntec.ui.component.ErrorPane
import zip.sora.ulearntec.ui.component.ListItemTag
import zip.sora.ulearntec.ui.mockClasses
import zip.sora.ulearntec.ui.mockLives
import zip.sora.ulearntec.ui.navigation.LocalNavAnimatedVisibilityScope
import zip.sora.ulearntec.ui.navigation.LocalSharedTransitionScope
import zip.sora.ulearntec.ui.theme.ULearnTecTheme
import java.time.Instant
import java.time.format.TextStyle

private enum class DownloadIndicatorType {
    NONE,
    LOADING,
    ERROR,
    START,
    DELETE,
    RESUME,
    PROGRESS
}

@androidx.annotation.OptIn(UnstableApi::class)
private fun getDownloadIndicatorType(uiState: ClassUiState): DownloadIndicatorType {
    if (uiState is ClassUiState.Detail.Loading) return DownloadIndicatorType.LOADING
    if (uiState is ClassUiState.Detail.Error) return DownloadIndicatorType.ERROR
    if (uiState is ClassUiState.Detail.Success) {
        val download = uiState.download ?: return DownloadIndicatorType.START
        val state = download.state
        return when (state) {
            Download.STATE_DOWNLOADING -> DownloadIndicatorType.PROGRESS
            Download.STATE_COMPLETED -> DownloadIndicatorType.DELETE
            Download.STATE_STOPPED -> DownloadIndicatorType.RESUME
            Download.STATE_FAILED -> DownloadIndicatorType.ERROR
            else -> DownloadIndicatorType.LOADING
        }
    }
    return DownloadIndicatorType.NONE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ClassScreen(
    uiState: ClassUiState,
    onRefresh: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onLiveClicked: (Live) -> Unit,
    onShowLiveDetail: (Live) -> Unit,
    onHideLiveDetail: () -> Unit,
    onDownloadClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val clazz = uiState.clazz
    val lives = uiState.lives

    var selectedStatus by rememberSaveable { mutableStateOf(LiveStatus.NONE) }

    var showClassDetailSheet by rememberSaveable { mutableStateOf(false) }
    if (showClassDetailSheet) {
        DetailSheet(
            title = R.string.class_detail,
            entries = listOf(
                DetailEntry(
                    name = R.string.class_id,
                    value = clazz.id,
                    icon = Icons.Filled.Tag
                ),
                DetailEntry(
                    name = R.string.class_name,
                    value = clazz.name,
                    icon = Icons.Filled.Class
                ),
                DetailEntry(
                    name = R.string.course_id,
                    value = clazz.courseId,
                    icon = Icons.Filled.Numbers
                ),
                DetailEntry(
                    name = R.string.course_name,
                    value = clazz.courseName,
                    icon = Icons.Filled.School
                ),
                DetailEntry(
                    name = R.string.cover,
                    value = clazz.cover,
                    icon = Icons.Filled.ImageAspectRatio,
                    isLink = true
                ),
                DetailEntry(
                    name = R.string.teacher_id,
                    value = clazz.teacherId,
                    icon = Icons.Filled.Badge,
                ),
                DetailEntry(
                    name = R.string.teacher_name,
                    value = clazz.teacherName,
                    icon = Icons.Filled.Person,
                ),
                DetailEntry(
                    name = R.string.teacher_username,
                    value = clazz.teacherUsername,
                    icon = Icons.Filled.AccountCircle,
                ),
                DetailEntry(
                    name = R.string.status,
                    value = stringResource(clazz.status.description),
                    icon = clazz.status.imageVector
                ),
                DetailEntry(
                    name = R.string.school_id,
                    value = clazz.schoolId,
                    icon = Icons.Filled.LocationCity
                ),
                DetailEntry(
                    name = R.string.school_name,
                    value = clazz.schoolName,
                    icon = Icons.Filled.School
                ),
                DetailEntry(
                    name = R.string.year,
                    value = clazz.year.toString(),
                    icon = Icons.Filled.CalendarToday
                ),
                DetailEntry(
                    name = R.string.semester,
                    value = clazz.num.toString(),
                    icon = Icons.AutoMirrored.Filled.ListAlt
                ),
                DetailEntry(
                    name = R.string.last_updated,
                    value = DateUtils.getRelativeTimeSpanString(
                        clazz.lastUpdated,
                        Instant.now().toEpochMilli(),
                        0
                    ).toString(),
                    icon = Icons.Filled.History
                )
            ),
            onDismissRequest = { showClassDetailSheet = false }
        )
    }

    if (uiState is ClassUiState.Detail) {
        val live = uiState.live
        DetailSheet(
            title = R.string.live_detail,
            entries = listOf(
                DetailEntry(
                    name = R.string.live_id,
                    value = live.id,
                    icon = Icons.Filled.Videocam
                ),
                DetailEntry(
                    name = R.string.resource_id,
                    value = "${live.resourceId}",
                    icon = Icons.AutoMirrored.Filled.Assignment
                ),
                DetailEntry(
                    name = R.string.week,
                    value = live.week.toString(),
                    icon = Icons.Filled.FormatListNumbered
                ),
                DetailEntry(
                    name = R.string.day_of_week,
                    value = live.weekday.getDisplayName(
                        TextStyle.SHORT,
                        Locale.current.platformLocale
                    ),
                    icon = Icons.Filled.CalendarMonth
                ),
                DetailEntry(
                    name = R.string.building_name,
                    value = "${live.buildingName}",
                    icon = Icons.Filled.Apartment
                ),
                DetailEntry(
                    name = R.string.room_name,
                    value = "${live.roomName}",
                    icon = Icons.Filled.MeetingRoom
                ),
                DetailEntry(
                    name = R.string.room_id,
                    value = "${live.roomId}",
                    icon = Icons.Filled.Numbers,
                ),
                DetailEntry(
                    name = R.string.room_type,
                    value = "${live.roomType}",
                    icon = Icons.Filled.Category,
                ),
                DetailEntry(
                    name = R.string.teacher_name,
                    value = live.teacherName,
                    icon = Icons.Filled.Person,
                ),
                DetailEntry(
                    name = R.string.course_id,
                    value = live.courseId,
                    icon = Icons.Filled.Numbers,
                ),
                DetailEntry(
                    name = R.string.course_name,
                    value = live.courseName,
                    icon = Icons.Filled.School,
                ),
                DetailEntry(
                    name = R.string.class_ids,
                    value = live.classId,
                    icon = Icons.Filled.Tag,
                ),
                DetailEntry(
                    name = R.string.class_names,
                    value = live.classNames,
                    icon = Icons.Filled.Groups,
                ),
                DetailEntry(
                    name = R.string.class_type,
                    value = live.classType,
                    icon = Icons.Filled.Category,
                ),
                DetailEntry(
                    name = R.string.section,
                    value = live.section,
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                ),
                DetailEntry(
                    name = R.string.time_range,
                    value = live.timeRange,
                    icon = Icons.Filled.Schedule,
                ),
                DetailEntry(
                    name = R.string.has_permission,
                    value = "${live.hasPermission}",
                    icon = Icons.Filled.Lock,
                ),
                DetailEntry(
                    name = R.string.is_released,
                    value = "${live.isReleased}",
                    icon = Icons.Filled.CloudDone,
                ),
                DetailEntry(
                    name = R.string.actionable,
                    value = "${live.isAction}",
                    icon = Icons.Filled.TouchApp,
                ),
                DetailEntry(
                    name = R.string.live_status,
                    value = stringResource(live.liveStatus.description),
                    icon = live.liveStatus.imageVector,
                ),
                DetailEntry(
                    name = R.string.video_length,
                    value = "${live.videoTimes}",
                    icon = Icons.Filled.AvTimer,
                ),
                DetailEntry(
                    name = R.string.scheduled_start_time,
                    value = DateUtils.formatDateTime(
                        context,
                        live.scheduleTimeStart,
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
                    ),
                    icon = Icons.Filled.PlayCircle,
                ),
                DetailEntry(
                    name = R.string.scheduled_end_time,
                    value = DateUtils.formatDateTime(
                        context,
                        live.scheduleTimeEnd,
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
                    ),
                    icon = Icons.Filled.StopCircle,
                ),
                DetailEntry(
                    name = R.string.last_updated,
                    value = DateUtils.getRelativeTimeSpanString(
                        live.lastUpdated,
                        Instant.now().toEpochMilli(),
                        0
                    ).toString(),
                    icon = Icons.Filled.History,
                ),
            ),
            onDismissRequest = onHideLiveDetail
        ) {
            LiveItem(live, modifier = Modifier.padding(horizontal = 24.dp), enabled = false)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        onHideLiveDetail()
                        onLiveClicked(live)
                    },
                    enabled = live.resourceId != null
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null
                    )
                }
                Button(
                    onClick = { onDownloadClicked() },
                    enabled = live.resourceId != null && uiState is ClassUiState.Detail.Success
                ) {
                    AnimatedContent(getDownloadIndicatorType(uiState)) { type ->
                        when (type) {
                            DownloadIndicatorType.LOADING -> CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )

                            DownloadIndicatorType.ERROR -> Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null
                            )

                            DownloadIndicatorType.START -> Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null
                            )

                            DownloadIndicatorType.PROGRESS -> CircularProgressIndicator(
                                progress = { (uiState as ClassUiState.Detail.Success).download!!.progress },
                                color = MaterialTheme.colorScheme.inversePrimary,
                                trackColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )

                            DownloadIndicatorType.DELETE -> Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null
                            )

                            DownloadIndicatorType.RESUME -> Icon(
                                imageVector = Icons.Rounded.Downloading,
                                contentDescription = null
                            )

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val showTitle by remember {
                        derivedStateOf {
                            scrollState.firstVisibleItemIndex > 0 ||
                                    scrollState.firstVisibleItemScrollOffset > 0
                        }
                    }
                    AnimatedVisibility(showTitle, enter = fadeIn(), exit = fadeOut()) {
                        Text(text = clazz.name)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClassDetailSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is ClassUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                LazyColumn(state = scrollState, modifier = Modifier.wrapContentHeight()) {
                    item(key = Int.MIN_VALUE, contentType = Int.MIN_VALUE) {
                        Box {
                            AsyncImage(
                                model = clazz.cover,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .blur(4.dp)
                                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                    .drawWithContent {
                                        val colors =
                                            listOf(Color.Black.copy(alpha = 0.8f), Color.Black)
                                        drawContent()
                                        drawRect(
                                            brush = Brush.verticalGradient(colors),
                                            blendMode = BlendMode.DstOut
                                        )
                                    }
                            )
                            Column(
                                modifier = Modifier
                                    .let {
                                        if (uiState !is ClassUiState.Error) it
                                        else it
                                            .fillParentMaxHeight()
                                            .padding(bottom = innerPadding.calculateBottomPadding())
                                    }
                                    .padding(top = innerPadding.calculateTopPadding() + 16.dp)
                            ) {
                                val navAnimatedVisibilityScope =
                                    LocalNavAnimatedVisibilityScope.current!!
                                with(LocalSharedTransitionScope.current!!) {
                                    ClassCardHorizontal(
                                        clazz = clazz,
                                        onClick = { showClassDetailSheet = true },
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .height(114.dp)
                                            .sharedElement(
                                                rememberSharedContentState("grid-${clazz.id}"),
                                                navAnimatedVisibilityScope
                                            )
                                            .sharedElement(
                                                rememberSharedContentState("list-${clazz.id}"),
                                                navAnimatedVisibilityScope
                                            )
                                    )
                                }
                                SelectionsChipRow(
                                    selectedStatus = selectedStatus,
                                    onStatusSelected = { selectedStatus = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (uiState is ClassUiState.Error) {
                                    ErrorPane(
                                        uiState.message(LocalContext.current),
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    val filteredLives = lives.filter {
                        (it.liveStatus == selectedStatus || selectedStatus == LiveStatus.NONE)
                    }


                    items(items = filteredLives, key = { it.id }) {
                        LiveItem(
                            onClick = { onLiveClicked(it) },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShowLiveDetail(it)
                            },
                            live = it,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem()
                        )
                    }

                    if (uiState !is ClassUiState.Error) {
                        item(
                            key = Int.MAX_VALUE,
                            contentType = Int.MAX_VALUE
                        ) { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionsChipRow(
    selectedStatus: LiveStatus,
    onStatusSelected: (LiveStatus) -> Unit,

    modifier: Modifier = Modifier
) {
    var statusSelectorExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Box {
            AssistChip(
                onClick = { statusSelectorExpanded = true },
                leadingIcon = {
                    AnimatedContent(selectedStatus) {
                        Icon(
                            imageVector = it.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                },
                label = {
                    AnimatedContent(selectedStatus) { Text(text = stringResource(it.description)) }
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
    }
}

@Composable
private fun StatusDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onStatusSelected: (LiveStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        LiveStatus.entries.forEach {
            DropdownMenuItem(
                text = { Text(text = stringResource(it.description)) },
                onClick = { onStatusSelected(it) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LiveItem(
    live: Live,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    ListItem(
        modifier = modifier.clip(RoundedCornerShape(12.dp)) then if (!enabled) Modifier else
            Modifier.combinedClickable(
                role = Role.Button,
                onClick = { if (live.resourceId != null) onClick() },
                onLongClick = onLongClick
            ),
        leadingContent = {
            Icon(
                imageVector = live.liveStatus.imageVector,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                text = live.liveRecordName,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = if (live.history == null) null else {
            {
                CircularProgressIndicator(
                    progress = { live.history.positionMillis / 1000 / live.videoTimes!!.toFloat() },
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        supportingContent = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ListItemTag(
                        icon = Icons.Outlined.Numbers,
                        text = stringResource(R.string.week_format, live.week)
                    )
                    ListItemTag(
                        icon = Icons.Outlined.CalendarMonth,
                        text = live.weekday.getDisplayName(
                            TextStyle.SHORT,
                            Locale.current.platformLocale
                        )
                    )
                    ListItemTag(
                        icon = Icons.Outlined.Room,
                        text = live.roomName ?: stringResource(R.string.none)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ClassScreenPreview() {
    ULearnTecTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility
                ) {
                    ClassScreen(
                        uiState = ClassUiState.Success(mockClasses[0], mockLives),
                        onRefresh = {},
                        onBackButtonClicked = {},
                        onLiveClicked = {},
                        onShowLiveDetail = {},
                        onHideLiveDetail = {},
                        onDownloadClicked = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ClassScreenErrorPreview() {
    ULearnTecTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility
                ) {
                    ClassScreen(
                        uiState = ClassUiState.Error(
                            mockClasses[0],
                            emptyList()
                        ) { "Human is dead, mismatch" },
                        onRefresh = {},
                        onBackButtonClicked = {},
                        onLiveClicked = {},
                        onShowLiveDetail = {},
                        onHideLiveDetail = {},
                        onDownloadClicked = {}
                    )
                }
            }
        }
    }
}
