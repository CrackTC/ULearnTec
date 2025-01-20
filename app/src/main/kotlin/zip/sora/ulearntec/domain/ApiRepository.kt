package zip.sora.ulearntec.domain

import zip.sora.ilearnapi.ILearnApi

interface ApiRepository {
    suspend fun getApi(): ILearnResult<ILearnApi>
    suspend fun login(username: String, password: String)
    suspend fun logout()
}