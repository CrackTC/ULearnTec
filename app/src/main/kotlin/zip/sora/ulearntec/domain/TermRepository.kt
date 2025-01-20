package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Term

interface TermRepository {
    suspend fun getAllTerms(): ILearnResult<List<Term>>
    suspend fun refresh(): ILearnResult<List<Term>>
}