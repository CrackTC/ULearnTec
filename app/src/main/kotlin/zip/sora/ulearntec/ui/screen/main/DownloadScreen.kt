package zip.sora.ulearntec.ui.screen.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.ResourceDownload
import zip.sora.ulearntec.domain.model.downloaded
import zip.sora.ulearntec.domain.model.progress
import zip.sora.ulearntec.domain.model.state
import zip.sora.ulearntec.domain.model.total
import zip.sora.ulearntec.ui.component.ErrorPane
import zip.sora.ulearntec.ui.component.ListItemTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    uiState: DownloadUiState,
    onRemove: (ResourceDownload) -> Unit,
    onResume: (ResourceDownload) -> Unit,
    onLongClick: (ResourceDownload) -> Unit,
    onPause: (ResourceDownload) -> Unit,
    onWatch: (Live) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.download)) }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.downloads.isEmpty()) {
            ErrorPane(
                stringResource(R.string.no_downloads_yet),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                itemsIndexed(
                    items = uiState.downloads,
                    key = { _, item -> item.resources.liveId }
                ) { index, download ->
                    val live =
                        if (uiState is DownloadUiState.Loading) null else uiState.lives[index]
                    DownloadItem(
                        onRemove = { onRemove(download) },
                        onResume = { onResume(download) },
                        onLongClick = { onLongClick(download) },
                        onPause = { onPause(download) },
                        onWatch = { if (live != null) onWatch(live) },
                        download = download,
                        live = live,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadItem(
    onResume: () -> Unit,
    onPause: () -> Unit,
    onWatch: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    download: ResourceDownload,
    live: Live?,
    modifier: Modifier = Modifier
) {
    val state = download.state
    val progress = remember { Animatable(0.0f) }
    LaunchedEffect(download.progress) {
        progress.animateTo(download.progress)
    }
    ListItem(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                role = Role.Button,
                onClick = {
                    when (state) {
                        Download.STATE_DOWNLOADING -> onPause()
                        Download.STATE_QUEUED -> onPause()
                        Download.STATE_STOPPED -> onResume()
                        Download.STATE_COMPLETED -> onWatch()
                    }
                },
                onLongClick = onLongClick
            ),
        leadingContent = {
            AnimatedContent(state) {
                when (it) {
                    Download.STATE_DOWNLOADING -> CircularProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.size(36.dp)
                    )

                    Download.STATE_COMPLETED -> Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )

                    Download.STATE_STOPPED -> Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )

                    Download.STATE_FAILED -> Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )

                    else -> CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }
        },
        headlineContent = {
            Text(
                text = live?.liveRecordName ?: stringResource(R.string.loading),
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
                    ListItemTag(
                        icon = Icons.Outlined.School,
                        text = live?.courseName ?: stringResource(R.string.loading)
                    )
                    ListItemTag(
                        icon = Icons.Outlined.Downloading,
                        text = "${download.downloaded / 1024 / 1024}MB / ${download.total / 1024 / 1024}MB"
                    )
                }
            }
        }
    )
}