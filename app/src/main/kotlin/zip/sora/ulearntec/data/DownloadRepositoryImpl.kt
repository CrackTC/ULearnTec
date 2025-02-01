package zip.sora.ulearntec.data

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import kotlinx.serialization.json.Json
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.ResourceDownload
import zip.sora.ulearntec.playback.ILearnDownloadService

@OptIn(UnstableApi::class)
class DownloadRepositoryImpl(
    private val downloadManager: DownloadManager
) : DownloadRepository {
    override fun downloadLive(context: Context, resources: LiveResources) {
        val data = Json.encodeToString(resources).toByteArray()
        val audioRequest = DownloadRequest.Builder(
            resources.audioPath,
            Uri.parse(resources.audioPath)
        ).setData(data).build()

        DownloadService.sendAddDownload(
            context,
            ILearnDownloadService::class.java,
            audioRequest,
            false
        )

        resources.videoList.forEach {
            val videoRequest = DownloadRequest.Builder(
                it.videoPath,
                Uri.parse(it.videoPath)
            ).setData(data).build()

            DownloadService.sendAddDownload(
                context,
                ILearnDownloadService::class.java,
                videoRequest,
                false
            )
        }

        if (resources.phaseUrl.isNotBlank()) {
            val phaseRequest = DownloadRequest.Builder(
                resources.phaseUrl,
                Uri.parse(resources.phaseUrl)
            ).setData(data).build()

            DownloadService.sendAddDownload(
                context,
                ILearnDownloadService::class.java,
                phaseRequest,
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
        if (download is ILearnResult.Error) {
            return ILearnResult.Error(download.error!!)
        }

        download.data!!.let {
            buildList {
                add(it.resources.audioPath)
                if (it.resources.phaseUrl.isNotBlank()) {
                    add(it.resources.phaseUrl)
                }
                addAll(it.resources.videoList.map { it.videoPath })
            }.forEach { id ->
                DownloadService.sendSetStopReason(
                    context,
                    ILearnDownloadService::class.java,
                    id,
                    reason,
                    false
                )
            }
        }

        return ILearnResult.Success(Unit)
    }

    override fun resumeDownload(context: Context, resources: LiveResources): ILearnResult<Unit> =
        pauseDownload(context, resources, reason = Download.STOP_REASON_NONE)

    override fun removeDownload(context: Context, resources: LiveResources): ILearnResult<Unit> {
        val download = getDownload(resources)
        if (download is ILearnResult.Error) {
            return ILearnResult.Error(download.error!!)
        }

        download.data!!.let {
            buildList {
                add(it.resources.audioPath)
                if (it.resources.phaseUrl.isNotBlank()) add(it.resources.phaseUrl)
                addAll(it.resources.videoList.map { it.videoPath })
            }.forEach { id ->
                DownloadService.sendRemoveDownload(
                    context,
                    ILearnDownloadService::class.java,
                    id,
                    false
                )
            }
        }

        return ILearnResult.Success(Unit)
    }

    override fun getDownload(resources: LiveResources): ILearnResult<ResourceDownload> {
        val index = downloadManager.downloadIndex

        val audioDownload = index.getDownload(resources.audioPath)
        val phaseDownload = index.getDownload(resources.phaseUrl)
        val videoDownloads = resources.videoList.map { index.getDownload(it.videoPath) }

        if (audioDownload == null || videoDownloads.any { it == null }) {
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
                    audioDownload = idDownloadMap[resources.audioPath]!!,
                    phaseDownload = idDownloadMap[resources.phaseUrl],
                    videoDownloads = resources.videoList.map { video -> idDownloadMap[video.videoPath]!! }
                )
            }
        }
    }
}