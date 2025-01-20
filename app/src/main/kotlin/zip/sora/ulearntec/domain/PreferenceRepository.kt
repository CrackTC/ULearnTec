package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Credential

interface PreferenceRepository {

    suspend fun getTecSession(): String?
    suspend fun setTecSession(session: String)
    suspend fun getResSession(): String?
    suspend fun setResSession(session: String)
    suspend fun clearSession()

    suspend fun isOutOfDate(lastUpdated: Long): Boolean
    suspend fun setRefreshIntervalMillis(millis: Long)
    suspend fun clearRefreshIntervalMillis()

    suspend fun isLoggedIn(): Boolean
    suspend fun setLoggedIn(loggedIn: Boolean)

    suspend fun getCredential(): ILearnResult<Credential>
    suspend fun updateCredential(credential: Credential)
    suspend fun clearCredential()
}