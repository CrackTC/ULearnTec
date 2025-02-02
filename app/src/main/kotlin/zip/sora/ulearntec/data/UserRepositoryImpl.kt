package zip.sora.ulearntec.data

import zip.sora.ilearnapi.auth.jlucas.CasAuthenticationException
import zip.sora.ulearntec.R
import zip.sora.ulearntec.data.local.dao.UserDao
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.domain.isError
import zip.sora.ulearntec.domain.model.User
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val apiRepository: ApiRepository,
    private val preferenceRepository: PreferenceRepository
) : UserRepository {

    override suspend fun getCurrentUser(): ILearnResult<User> {
        val localUser = userDao.getCurrentUser()
        if (localUser == null || preferenceRepository.isOutOfDate(localUser.lastUpdated)) {
            val remoteUser = refresh()
            if (remoteUser is ILearnResult.Success) return remoteUser
        }
        if (localUser != null)
            return ILearnResult.Success(localUser.toUser())
        return ILearnResult.Error { it.getString(R.string.failed_to_fetch_user_info) }
    }

    override suspend fun refresh(): ILearnResult<User> {
        apiRepository.getApi().let { res ->
            if (res.isError()) return ILearnResult.Error(res.error)

            try {
                val remoteUser = res.data.tecService.getSelf()
                userDao.upsert(remoteUser.toUserEntity(Instant.now().toEpochMilli()))
                return ILearnResult.Success(userDao.getCurrentUser()!!.toUser())
            } catch (e: CancellationException) {
                throw e
            } catch (e: CasAuthenticationException) {
                return ILearnResult.Error { it.getString(R.string.wrong_username_or_password) }
            } catch (e: Exception) {
                return ILearnResult.Error { e.stackTraceToString() }
            }
        }
    }

    override suspend fun isLoggedIn(): Boolean =
        preferenceRepository.isLoggedIn()

    override suspend fun login(username: String, password: String): ILearnResult<User> {
        apiRepository.login(username, password)

        return refresh().also {
            if (it is ILearnResult.Success)
                preferenceRepository.setLoggedIn(true)
        }
    }

    override suspend fun logout() {
        apiRepository.logout()
        preferenceRepository.setLoggedIn(false)
        userDao.clear()
    }
}