package zip.sora.ulearntec.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import kotlinx.serialization.json.Json
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.ResourceDownload
import zip.sora.ulearntec.playback.ILearnDownloadService

@OptIn(UnstableApi::class)
class DownloadRepositoryImpl(
    private val downloadManager: DownloadManager
) : DownloadRepository {
    private fun LiveResources.buildResourcesList(): List<String> = buildList {
        if (audioPath.isNotBlank()) add(audioPath)
        if (phaseUrl.isNotBlank()) add(phaseUrl)
        addAll(videoList.map { it.videoPath })
    }

    override fun downloadLive(context: Context, resources: LiveResources) {
        val data = Json.encodeToString(resources).toByteArray()

        resources.buildResourcesList().forEach {
            val request = DownloadRequest.Builder(it, it.toUri())
                .setData(data)
                .build()

            DownloadService.sendAddDownload(
                context,
                ILearnDownloadService::class.java,
                request,
                false
            )
        }
    }

    override fun pauseDownload(
        context: Context,
        resources: LiveResources,
        reason: Int
    ): ILearnResult<Unit> {
        val download = getDownload(resources)
        if (download.isError()) return ILearnResult.Error(download.error)

        resources.buildResourcesList().forEach { id ->
            DownloadService.sendSetStopReason(
                context,
                ILearnDownloadService::class.java,
                id,
                reason,
                false
            )
        }

        return ILearnResult.Success(Unit)
    }

    override fun resumeDownload(context: Context, resources: LiveResources): ILearnResult<Unit> =
        pauseDownload(context, resources, reason = Download.STOP_REASON_NONE)

    override fun removeDownload(context: Context, resources: LiveResources): ILearnResult<Unit> {
        val download = getDownload(resources)
        if (download.isError()) return ILearnResult.Error(download.error)

        resources.buildResourcesList().forEach { id ->
            DownloadService.sendRemoveDownload(
                context,
                ILearnDownloadService::class.java,
                id,
                false
            )
        }

        return ILearnResult.Success(Unit)
    }

    override fun getDownload(resources: LiveResources): ILearnResult<ResourceDownload> {
        val index = downloadManager.downloadIndex

        val audioDownload = index.getDownload(resources.audioPath)
        val phaseDownload = index.getDownload(resources.phaseUrl)
        val videoDownloads = resources.videoList.map { index.getDownload(it.videoPath) }

        if (videoDownloads.any { it == null }) {
            return ILearnResult.Error { it.getString(R.string.no_download_found_for_resource) }
        }

        return ILearnResult.Success(
            ResourceDownload(
                resources = resources,
                audioDownload = audioDownload,
                phaseDownload = phaseDownload,
                videoDownloads = videoDownloads.filterNotNull()
            )
        )
    }

    override fun getAllDownloads(): List<ResourceDownload> {
        val index = downloadManager.downloadIndex
        index.getDownloads().use {
            val downloads = buildList {
                while (it.moveToNext()) {
                    add(it.download)
                }
            }

            return downloads.groupBy { it.request.data.decodeToString() }.map {
                val resources: LiveResources = Json.decodeFromString(it.key)
                val idDownloadMap = it.value.associateBy { download -> download.request.id }

                ResourceDownload(
                    resources = resources,
                    audioDownload = idDownloadMap[resources.audioPath],
                    phaseDownload = idDownloadMap[resources.phaseUrl],
                    videoDownloads = resources.videoList.map { video -> idDownloadMap[video.videoPath]!! }
                )
            }
        }
    }
}