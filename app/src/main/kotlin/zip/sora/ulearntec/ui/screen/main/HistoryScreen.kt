package zip.sora.ulearntec.ui.screen.main

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.ui.component.ErrorPane
import zip.sora.ulearntec.ui.component.ListItemTag
import zip.sora.ulearntec.ui.mockLives
import zip.sora.ulearntec.ui.theme.ULearnTecTheme
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onLiveClicked: (Live) -> Unit,
    onRemove: (Live) -> Unit,
    onRefresh: () -> Unit,
    onGotoClass: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text(text = "History") }) }) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is HistoryUiState.Loading,
            onRefresh = onRefresh,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState is HistoryUiState.Success) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(items = uiState.allLivesWithHistory, key = { it.id }) {
                        HistoryItem(
                            onClick = { onLiveClicked(it) },
                            onLongClick = { onGotoClass(it.classId) },
                            onRemove = { onRemove(it) },
                            live = it
                        )
                    }
                }
            }

            if (uiState is HistoryUiState.Error) {
                ErrorPane(
                    uiState.message(context),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    live: Live,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                role = Role.Button,
                onClick = { if (live.resourceId != null) onClick() else onLongClick() },
                onLongClick = onLongClick
            ),
        leadingContent = {
            if (live.history != null && live.videoTimes != null) { // likely
                CircularProgressIndicator(
                    progress = { live.history.positionMillis / 1000 / live.videoTimes.toFloat() },
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        headlineContent = {
            Text(
                text = live.liveRecordName,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null
                )
            }
        },
        supportingContent = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (live.history != null) {
                        ListItemTag(
                            icon = Icons.Outlined.Schedule,
                            text = DateUtils.getRelativeTimeSpanString(
                                live.history.timestamp,
                                Instant.now().toEpochMilli(),
                                0
                            ).toString()
                        )
                    }
                    ListItemTag(
                        icon = Icons.Outlined.School,
                        text = live.courseName
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    ULearnTecTheme {
        HistoryScreen(
            uiState = HistoryUiState.Success(mockLives),
            onLiveClicked = {},
            onRefresh = {},
            onRemove = {},
            onGotoClass = {}
        )
    }
}

@Preview
@Composable
private fun HistoryScreenErrorPreview() {
    ULearnTecTheme {
        HistoryScreen(
            uiState = HistoryUiState.Error { "Human is dead, mismatch" },
            onLiveClicked = {},
            onRefresh = {},
            onRemove = {},
            onGotoClass = {}
        )
    }
}