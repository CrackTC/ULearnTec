package zip.sora.ulearntec.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import org.koin.android.ext.android.inject
import zip.sora.ulearntec.DOWNLOAD_FOREGROUND_NOTIFICATION_ID
import zip.sora.ulearntec.DOWNLOAD_WORK_NAME
import zip.sora.ulearntec.R

@OptIn(UnstableApi::class)
class ILearnDownloadService : DownloadService(DOWNLOAD_FOREGROUND_NOTIFICATION_ID) {
    private val downloadNotificationHelper: DownloadNotificationHelper by inject()
    private val _downloadManager: DownloadManager by inject()

    override fun getDownloadManager() = _downloadManager

    override fun getScheduler() = WorkManagerScheduler(this, DOWNLOAD_WORK_NAME)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ) = downloadNotificationHelper.buildProgressNotification(
        this,
        R.drawable.ic_download,
        null,
        null,
        downloads,
        notMetRequirements
    )
}