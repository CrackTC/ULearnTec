package zip.sora.ulearntec.data

import zip.sora.ulearntec.R
import zip.sora.ulearntec.data.local.dao.LiveResourcesDao
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException


class LiveResourcesRepositoryImpl(
    private val liveResourcesDao: LiveResourcesDao,
    private val preferenceRepository: PreferenceRepository,
    private val apiRepository: ApiRepository
) : LiveResourcesRepository {

    private lateinit var currentLive: Live

    private suspend fun refreshWithLive(live: Live): ILearnResult<LiveResources> {
        apiRepository.getApi().let { res ->
            if (res is ILearnResult.Error) return ILearnResult.Error(res.error)

            try {
                val remoteResources = res.data!!.resService.getLiveVideos(live.resourceId!!)
                val lastUpdated = Instant.now().toEpochMilli()
                liveResourcesDao.refresh(
                    remoteResources.toLiveResourcesEntity(live.resourceId, lastUpdated),
                    remoteResources.videoList.map { it.toVideoEntity(live.resourceId, lastUpdated) }
                )
                return ILearnResult.Success(
                    liveResourcesDao.getLiveResources(live.resourceId)!!
                        .toLiveResources(liveResourcesDao.getVideos(live.resourceId))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return ILearnResult.Error { e.stackTraceToString() }
            }
        }
    }

    override suspend fun getLiveResources(): ILearnResult<LiveResources> {
        if (!::currentLive.isInitialized) return ILearnResult.Error { it.getString(R.string.current_live_is_not_set) }

        val live = synchronized(this) { currentLive }
        val localResources = liveResourcesDao.getLiveResources(live.resourceId!!)
        val localVideoList = liveResourcesDao.getVideos(live.resourceId)
        if (localResources == null || preferenceRepository.isOutOfDate(localResources.lastUpdated)) {
            val remoteResources = refreshWithLive(live)
            if (remoteResources is ILearnResult.Success) return remoteResources
        }
        if (localResources != null)
            return ILearnResult.Success(localResources.toLiveResources(localVideoList))

        return ILearnResult.Error { it.getString(R.string.failed_to_fetch_live_resources) }
    }

    override suspend fun setCurrentLive(live: Live) =
        synchronized(this) { currentLive = live }

    override suspend fun refresh() =
        if (::currentLive.isInitialized) refreshWithLive(synchronized(this) { currentLive })
        else ILearnResult.Error { it.getString(R.string.current_live_is_not_set) }
}