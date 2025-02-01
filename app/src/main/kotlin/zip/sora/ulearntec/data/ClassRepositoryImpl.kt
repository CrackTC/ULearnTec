package zip.sora.ulearntec.data

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

    override suspend fun refresh(term: Term): ILearnResult<List<Class>> {
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

    override suspend fun getTermClasses(term: Term): ILearnResult<List<Class>> {
        val localClasses = classDao.getTermClasses(term.year, term.num)
        if (localClasses.isEmpty() || preferenceRepository.isOutOfDate(localClasses[0].lastUpdated)) {
            val remoteClasses = refresh(term)
            if (remoteClasses is ILearnResult.Success) {
                return remoteClasses
            }
        }
        return ILearnResult.Success(localClasses.map { it.toClass() })
    }
}