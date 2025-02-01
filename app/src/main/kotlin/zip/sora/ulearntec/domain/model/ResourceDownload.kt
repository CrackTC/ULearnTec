package zip.sora.ulearntec.domain.model

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import zip.sora.ulearntec.DOWNLOAD_WORK_NAME

@OptIn(UnstableApi::class)
data class ResourceDownload(
    val resources: LiveResources,
    val audioDownload: Download,
    val phaseDownload: Download?,
    val videoDownloads: List<Download>,
)

private val Download?.contentLengthOrZero
    @OptIn(UnstableApi::class)
    get() = when {
        this == null -> 0
        contentLength == C.LENGTH_UNSET.toLong() -> bytesDownloaded
        else -> contentLength
    }

val ResourceDownload.downloaded: Long
    @OptIn(UnstableApi::class)
    get() = audioDownload.bytesDownloaded +
            (phaseDownload?.bytesDownloaded ?: 0) +
            videoDownloads.sumOf { it.bytesDownloaded }

val ResourceDownload.total: Long
    @OptIn(UnstableApi::class)
    get() = audioDownload.contentLengthOrZero +
            phaseDownload.contentLengthOrZero +
            videoDownloads.sumOf { it.contentLengthOrZero }


val ResourceDownload.progress: Float
    @OptIn(UnstableApi::class)
    get() {
        val result = downloaded / total.toFloat()
        return if (result.isNaN()) 0.0f else result
    }

val ResourceDownload.state: Int
    @OptIn(UnstableApi::class)
    get() {
        val stateList = buildList {
            add(audioDownload.state)
            addAll(videoDownloads.map { it.state })
            if (phaseDownload != null) add(phaseDownload.state)
        }

        if (stateList.all { it == Download.STATE_COMPLETED }) return Download.STATE_COMPLETED
        if (stateList.any { it == Download.STATE_FAILED }) return Download.STATE_FAILED
        if (stateList.any { it == Download.STATE_REMOVING }) return Download.STATE_REMOVING
        if (stateList.any { it == Download.STATE_RESTARTING }) return Download.STATE_RESTARTING
        if (stateList.any { it == Download.STATE_DOWNLOADING }) return Download.STATE_DOWNLOADING
        if (stateList.any { it == Download.STATE_QUEUED }) return Download.STATE_QUEUED
        return Download.STATE_STOPPED
    }