package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): ILearnResult<User>
    suspend fun refresh(): ILearnResult<User>
    suspend fun isLoggedIn(): Boolean
    suspend fun login(username: String, password: String): ILearnResult<User>
    suspend fun logout()
}