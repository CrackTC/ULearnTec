package zip.sora.ulearntec.data

import zip.sora.ulearntec.R
import zip.sora.ulearntec.data.local.dao.ClassDao
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Term
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

class ClassRepositoryImpl(
    private val classDao: ClassDao,
    private val preferenceRepository: PreferenceRepository,
    private val apiRepository: ApiRepository
) : ClassRepository {

    private lateinit var currentTerm: Term

    private suspend fun refreshWithTerm(term: Term): ILearnResult<List<Class>> {
        apiRepository.getApi().let { res ->
            if (res is ILearnResult.Error) {
                return ILearnResult.Error(res.error)
            }

            try {
                val remoteClasses =
                    res.data!!.tecService.getTermClasses(term.year, term.num)
                val lastUpdated = Instant.now().toEpochMilli()
                classDao.refresh(
                    term.year,
                    term.num,
                    remoteClasses.map { it.toEntity(term.year, term.num, lastUpdated) })
                return ILearnResult.Success(
                    classDao.getTermClasses(term.year, term.num).map { it.toClass() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return ILearnResult.Error({ e.stackTraceToString() })
            }
        }
    }

    override suspend fun getClass(classId: String): Class? {
        return classDao.getClass(classId)?.toClass()
    }

    override suspend fun getTermClasses(): ILearnResult<List<Class>> {
        if (!::currentTerm.isInitialized) {
            return ILearnResult.Error({ it.getString(R.string.current_term_is_not_set) })
        }

        val term = synchronized(this) { currentTerm }

        val localClasses = classDao.getTermClasses(term.year, term.num)
        if (localClasses.isEmpty() || preferenceRepository.isOutOfDate(localClasses[0].lastUpdated)) {
            val remoteClasses = refreshWithTerm(term)
            if (remoteClasses is ILearnResult.Success) {
                return remoteClasses
            }
        }
        return ILearnResult.Success(localClasses.map { it.toClass() })
    }

    override suspend fun setCurrentTerm(term: Term) =
        synchronized(this) { currentTerm = term }

    override suspend fun refresh() =
        if (::currentTerm.isInitialized) refreshWithTerm(synchronized(this) { currentTerm })
        else ILearnResult.Error({ it.getString(R.string.current_term_is_not_set) })
}