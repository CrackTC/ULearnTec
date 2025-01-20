package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveResources

interface LiveResourcesRepository {
    suspend fun getLiveResources(): ILearnResult<LiveResources>
    suspend fun setCurrentLive(live: Live)
    suspend fun refresh(): ILearnResult<LiveResources>
}