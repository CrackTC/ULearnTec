package zip.sora.ulearntec.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import zip.sora.ulearntec.data.local.entity.TermEntity

@Dao
interface TermDao {
    @Upsert
    suspend fun upsert(terms: List<TermEntity>)

    @Query("DELETE FROM term WHERE (year, num) = (:year, :num)")
    suspend fun delete(year: Int, num: Int)

    @Transaction
    suspend fun refresh(terms: List<TermEntity>) {
        val old = getAllTerms().map { Pair(it.year, it.num) }.toSet()
        val new = terms.map { Pair(it.year, it.num) }.toSet()
        (old - new).forEach { delete(it.first, it.second) }
        upsert(terms)
    }

    @Query("SELECT * FROM term WHERE (year, num) = (:year, :num)")
    suspend fun getTerm(year: Int, num: Int): TermEntity?

    @Query("SELECT * FROM term ORDER BY year, num")
    suspend fun getAllTerms(): List<TermEntity>
}