package zip.sora.ulearntec.domain

import android.content.Context
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.ResourceDownload

interface DownloadRepository {
    fun downloadLive(context: Context, resources: LiveResources)
    fun pauseDownload(context: Context, resources: LiveResources, reason: Int = 1): ILearnResult<Unit>
    fun resumeDownload(context: Context, resources: LiveResources): ILearnResult<Unit>
    fun removeDownload(context: Context, resources: LiveResources): ILearnResult<Unit>
    fun getDownload(resources: LiveResources): ILearnResult<ResourceDownload>
    fun getAllDownloads(): List<ResourceDownload>
}