package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.Term

interface ClassRepository {
    suspend fun getClass(classId: String): Class?
    suspend fun getTermClasses(term: Term): ILearnResult<List<Class>>
    suspend fun refresh(term: Term): ILearnResult<List<Class>>
}