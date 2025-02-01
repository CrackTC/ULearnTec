package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveResources

interface LiveResourcesRepository {
    suspend fun getLiveResources(live: Live): ILearnResult<LiveResources>
    suspend fun refresh(live: Live): ILearnResult<LiveResources>
}