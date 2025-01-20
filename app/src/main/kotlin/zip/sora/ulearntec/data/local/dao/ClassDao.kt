package zip.sora.ulearntec.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import zip.sora.ulearntec.data.local.entity.ClassEntity

@Dao
interface ClassDao {
    @Upsert
    suspend fun upsert(classes: List<ClassEntity>)

    @Query("DELETE FROM class WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    suspend fun refresh(year: Int, num: Int, classes: List<ClassEntity>) {
        val old = getTermClasses(year, num).map { it.id }.toSet()
        val new = classes.map { it.id }.toSet()
        (old - new).forEach { delete(it) }
        upsert(classes)
    }

    @Query("SELECT * FROM class WHERE (year, num) = (:year, :num)")
    suspend fun getTermClasses(year: Int, num: Int): List<ClassEntity>

    @Query("SELECT * FROM class WHERE id = :id")
    suspend fun getClass(id: String): ClassEntity?
}