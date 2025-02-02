package zip.sora.ulearntec.data

import zip.sora.ulearntec.R
import zip.sora.ulearntec.data.local.dao.LiveDao
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.LiveHistory
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class LiveRepositoryImpl(
    private val liveDao: LiveDao,
    private val preferenceRepository: PreferenceRepository,
    private val apiRepository: ApiRepository
) : LiveRepository {

    override suspend fun refresh(clazz: Class): ILearnResult<List<Live>> {
        apiRepository.getApi().let { res ->
            if (res.isError()) return ILearnResult.Error(res.error)

            try {
                val remoteLives = res.data.tecService.getClassLives(clazz.id)
                val lastUpdated = Instant.now().toEpochMilli()
                liveDao.refresh(
                    clazz.id,
                    remoteLives.map { it.toLiveEntity(clazz.id, lastUpdated) }
                )
                return ILearnResult.Success(liveDao.getClassLives(clazz.id).map { it.toLive() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return ILearnResult.Error { e.stackTraceToString() }
            }
        }
    }

    override suspend fun getLive(liveId: String): ILearnResult<Live> {
        val live = liveDao.getLive(liveId)
            ?: return ILearnResult.Error { it.getString(R.string.live_not_found, liveId) }

        return ILearnResult.Success(live.toLive())
    }

    override suspend fun getClassLives(clazz: Class): ILearnResult<List<Live>> {
        val localLives = liveDao.getClassLives(clazz.id)

        if (localLives.isEmpty() || preferenceRepository.isOutOfDate(localLives[0].live.lastUpdated)) {
            val remoteLives = refresh(clazz)
            if (remoteLives.isError()) {
                if (localLives.isEmpty()) return ILearnResult.Error(remoteLives.error)
            } else return remoteLives
        }

        return ILearnResult.Success(localLives.map { it.toLive() })
    }

    override suspend fun updateHistory(liveHistory: LiveHistory) {
        liveDao.upsertHistory(liveHistory.toLiveHistoryEntity())
    }

    override suspend fun removeHistory(liveHistory: LiveHistory) {
        liveDao.deleteHistory(liveHistory.toLiveHistoryEntity())
    }

    override suspend fun getAllLivesWithHistory(): List<Live> {
        return liveDao.getAllLivesWithHistory().map { it.toLive() }
    }
}