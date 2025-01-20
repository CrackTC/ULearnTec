package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveHistory

interface LiveRepository {
    suspend fun getLive(liveId: String): ILearnResult<Live>
    suspend fun getClassLives(): ILearnResult<List<Live>>
    suspend fun setCurrentClass(clazz: Class)
    suspend fun updateHistory(liveHistory: LiveHistory)
    suspend fun removeHistory(liveHistory: LiveHistory)
    suspend fun getAllLivesWithHistory(): List<Live>
    suspend fun refresh(): ILearnResult<List<Live>>
}