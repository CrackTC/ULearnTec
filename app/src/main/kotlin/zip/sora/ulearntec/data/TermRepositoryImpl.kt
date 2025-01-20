package zip.sora.ulearntec.data

import zip.sora.ulearntec.data.local.dao.TermDao
import zip.sora.ulearntec.data.local.dao.UserDao
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.model.Term
import zip.sora.ulearntec.domain.TermRepository
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class TermRepositoryImpl(
    private val userDao: UserDao,
    private val termDao: TermDao,
    private val preferenceRepository: PreferenceRepository,
    private val apiRepository: ApiRepository
) : TermRepository {

    override suspend fun getAllTerms(): ILearnResult<List<Term>> {
        val localAllTerms = termDao.getAllTerms()
        if (localAllTerms.isEmpty() || preferenceRepository.isOutOfDate(localAllTerms[0].lastUpdated)) {
            val remoteAllTerms = refresh()
            if (remoteAllTerms is ILearnResult.Success) return remoteAllTerms
        }
        return ILearnResult.Success(localAllTerms.map { it.toTerm() })
    }

    override suspend fun refresh(): ILearnResult<List<Term>> {
        apiRepository.getApi().let { res ->
            if (res is ILearnResult.Error)
                return ILearnResult.Error(res.error)

            try {
                val remoteAllTerms = res.data!!.tecService.getTerms()
                val user = userDao.getCurrentUser()!!
                val lastUpdated = Instant.now().toEpochMilli()
                termDao.refresh(remoteAllTerms.map { it.toTermEntity(user.studentId, lastUpdated) })
                return ILearnResult.Success(termDao.getAllTerms().map { it.toTerm() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return ILearnResult.Error { e.stackTraceToString() }
            }
        }
    }
}