package zip.sora.ulearntec.ui.screen

import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.text.format.DateUtils.WEEK_IN_MILLIS
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.DEFAULT_DATA_EXPIRE_MILLIS
import zip.sora.ulearntec.DEFAULT_LONG_PRESS_SPEED
import zip.sora.ulearntec.DEFAULT_MAX_PLAYER_CACHE_MB
import zip.sora.ulearntec.DEFAULT_SWIPE_BRIGHTNESS_PERCENT
import zip.sora.ulearntec.DEFAULT_SWIPE_SEEK_FIXED_MILLIS
import zip.sora.ulearntec.DEFAULT_SWIPE_SEEK_PERCENT
import zip.sora.ulearntec.DEFAULT_SWIPE_VOLUME_PERCENT
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.PlayerTheme
import zip.sora.ulearntec.domain.SwipeSeekMode
import zip.sora.ulearntec.domain.Theme
import zip.sora.ulearntec.ui.theme.ULearnTecTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onDataExpireMillisChanged: (Long) -> Unit,
    onThemeChanged: (Theme) -> Unit,
    onMaxPlayerCacheMbChanged: (Long) -> Unit,
    onPlayerThemeChanged: (PlayerTheme) -> Unit,
    onSwipeSeekModeChanged: (SwipeSeekMode) -> Unit,
    onSwipeSeekFixedMillisChanged: (Long) -> Unit,
    onSwipeSeekPercentChanged: (Float) -> Unit,
    onSwipeVolumePercentChanged: (Float) -> Unit,
    onSwipeBrightnessPercentChanged: (Float) -> Unit,
    onLongPressSpeedChanged: (Float) -> Unit,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val context = LocalContext.current
        if (uiState !is SettingsUiState.Success) return@Scaffold
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Header(text = stringResource(R.string.network))
            SelectionSettingEntry(
                title = stringResource(R.string.data_expire_time),
                currentEntry = uiState.dataExpireMillis,
                entries = listOf(
                    1L,
                    6 * HOUR_IN_MILLIS,
                    12 * HOUR_IN_MILLIS,
                    DAY_IN_MILLIS,
                    WEEK_IN_MILLIS,
                    0L
                ),
                descriptionSelector = {
                    when {
                        it == 0L -> context.getString(R.string.never)
                        it == 1L -> context.getString(R.string.always)
                        it >= WEEK_IN_MILLIS -> context.resources.getQuantityString(
                            R.plurals.weeks_format,
                            (it / WEEK_IN_MILLIS).toInt(),
                            it / WEEK_IN_MILLIS
                        )
                        it >= DAY_IN_MILLIS -> context.resources.getQuantityString(
                            R.plurals.days_format,
                            (it / DAY_IN_MILLIS).toInt(),
                            it / DAY_IN_MILLIS
                        )

                        it >= HOUR_IN_MILLIS -> context.resources.getQuantityString(
                            R.plurals.hours_format,
                            (it / HOUR_IN_MILLIS).toInt(),
                            it / HOUR_IN_MILLIS
                        )
                        else -> context.getString(R.string.ms_format, it)
                    }
                },
                onEntrySelected = onDataExpireMillisChanged
            )
            Header(text = stringResource(R.string.appearance))
            SelectionSettingEntry(
                title = stringResource(R.string.theme),
                currentEntry = uiState.theme,
                entries = Theme.entries,
                descriptionSelector = {
                    when (it) {
                        Theme.SYSTEM -> context.getString(R.string.follow_system)
                        Theme.LIGHT -> context.getString(R.string.light)
                        Theme.DARK -> context.getString(R.string.dark)
                    }
                },
                onEntrySelected = onThemeChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.player_theme),
                currentEntry = uiState.playerTheme,
                entries = PlayerTheme.entries,
                descriptionSelector = {
                    when (it) {
                        PlayerTheme.FOLLOW_THEME -> context.getString(R.string.follow_theme_setting)
                        PlayerTheme.SYSTEM -> context.getString(R.string.follow_system)
                        PlayerTheme.LIGHT -> context.getString(R.string.light)
                        PlayerTheme.DARK -> context.getString(R.string.dark)
                    }
                },
                onEntrySelected = onPlayerThemeChanged
            )
            Header(text = stringResource(R.string.storage))
            SelectionSettingEntry(
                title = stringResource(R.string.max_player_cache_size),
                currentEntry = uiState.maxPlayerCacheMb,
                entries = listOf(
                    0L,
                    64L,
                    256L,
                    1024L,
                    4096L
                ),
                descriptionSelector = { "${it}MB" },
                onEntrySelected = onMaxPlayerCacheMbChanged
            )
            Header(text = stringResource(R.string.player))
            SelectionSettingEntry(
                title = stringResource(R.string.swipe_seek_mode),
                currentEntry = uiState.swipeSeekMode,
                entries = SwipeSeekMode.entries,
                descriptionSelector = {
                    when (it) {
                        SwipeSeekMode.FIXED -> context.getString(R.string.fixed_time)
                        SwipeSeekMode.PERCENT -> context.getString(R.string.video_percent)
                    }
                },
                onEntrySelected = onSwipeSeekModeChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.swipe_seek_fixed_time),
                currentEntry = uiState.swipeSeekFixedMillis,
                entries = listOf(
                    15_000L,
                    30_000L,
                    60_000L,
                    120_000L,
                    240_000L
                ),
                descriptionSelector = {
                    context.resources.getQuantityString(
                        R.plurals.seconds_format,
                        (it / SECOND_IN_MILLIS).toInt(),
                        it / SECOND_IN_MILLIS
                    )
                },
                onEntrySelected = onSwipeSeekFixedMillisChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.swipe_seek_video_percent),
                currentEntry = uiState.swipeSeekPercent,
                entries = listOf(
                    0.005f,
                    0.01f,
                    0.02f,
                    0.04f,
                    0.08f,
                    0.16f,
                    0.32f,
                    0.64f
                ),
                descriptionSelector = { "${it * 100}%" },
                onEntrySelected = onSwipeSeekPercentChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.swipe_volume_adjust_percent),
                currentEntry = uiState.swipeVolumePercent,
                entries = listOf(
                    0.25f,
                    0.5f,
                    1.0f,
                    2.0f,
                ),
                descriptionSelector = { "${it * 100}%" },
                onEntrySelected = onSwipeVolumePercentChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.swipe_brightness_adjust_percent),
                currentEntry = uiState.swipeBrightnessPercent,
                entries = listOf(
                    0.25f,
                    0.5f,
                    1.0f,
                    2.0f,
                ),
                descriptionSelector = { "${it * 100}%" },
                onEntrySelected = onSwipeBrightnessPercentChanged
            )
            SelectionSettingEntry(
                title = stringResource(R.string.long_press_speed),
                currentEntry = uiState.longPressSpeed,
                entries = listOf(
                    1.25f,
                    1.5f,
                    2.0f,
                    4.0f,
                    8.0f
                ),
                descriptionSelector = { "${it}x" },
                onEntrySelected = onLongPressSpeedChanged
            )
        }
    }
}

@Composable
fun <T> SelectionSettingEntry(
    title: String,
    currentEntry: T,
    entries: List<T>,
    descriptionSelector: (T) -> String,
    onEntrySelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = {
            Text(
                text = descriptionSelector(currentEntry),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            title = { Text(text = title) },
            text = {
                LazyColumn {
                    items(items = entries) { entry ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEntrySelected(entry)
                                    showDialog = false
                                }
                        ) {
                            RadioButton(
                                selected = entry == currentEntry,
                                onClick = {
                                    onEntrySelected(entry)
                                    showDialog = false
                                }
                            )
                            Text(text = descriptionSelector(entry))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun Header(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview
@Composable
private fun SettingScreenPreview() {
    ULearnTecTheme {
        SettingsScreen(
            uiState = SettingsUiState.Success(
                DEFAULT_DATA_EXPIRE_MILLIS,
                DEFAULT_MAX_PLAYER_CACHE_MB,
                Theme.SYSTEM,
                PlayerTheme.FOLLOW_THEME,
                SwipeSeekMode.FIXED,
                DEFAULT_SWIPE_SEEK_FIXED_MILLIS,
                DEFAULT_SWIPE_SEEK_PERCENT,
                DEFAULT_SWIPE_VOLUME_PERCENT,
                DEFAULT_SWIPE_BRIGHTNESS_PERCENT,
                DEFAULT_LONG_PRESS_SPEED
            ),
            onBackButtonClicked = {},
            onDataExpireMillisChanged = {},
            onThemeChanged = {},
            onPlayerThemeChanged = {},
            onMaxPlayerCacheMbChanged = {},
            onSwipeSeekModeChanged = {},
            onSwipeSeekFixedMillisChanged = {},
            onSwipeSeekPercentChanged = {},
            onSwipeVolumePercentChanged = {},
            onSwipeBrightnessPercentChanged = {},
            onLongPressSpeedChanged = {}
        )
    }
}