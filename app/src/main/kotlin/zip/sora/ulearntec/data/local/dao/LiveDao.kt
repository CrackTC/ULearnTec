package zip.sora.ulearntec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import zip.sora.ulearntec.data.local.entity.LiveEntity
import zip.sora.ulearntec.data.local.entity.LiveHistoryEntity
import zip.sora.ulearntec.data.local.relation.LiveHistoryRelation

@Dao
interface LiveDao {
    @Upsert
    suspend fun upsert(lives: List<LiveEntity>)

    @Query("DELETE FROM live WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    suspend fun refresh(classId: String, lives: List<LiveEntity>) {
        val old = getClassLives(classId).map { it.live.id }.toSet()
        val new = lives.map { it.id }.toSet()
        (old - new).forEach { delete(it) }
        upsert(lives)
    }

    @Upsert
    suspend fun upsertHistory(history: LiveHistoryEntity)

    @Delete
    suspend fun deleteHistory(history: LiveHistoryEntity)

    @Transaction
    @Query("SELECT * FROM live WHERE class_id = :classId")
    suspend fun getClassLives(classId: String): List<LiveHistoryRelation>

    @Transaction
    @Query("SELECT * FROM live WHERE id = :id")
    suspend fun getLive(id: String): LiveHistoryRelation?

    @Transaction
    @Query("SELECT live.* FROM live INNER JOIN live_history ON live.id = live_history.live_id ORDER BY live_history.timestamp DESC")
    suspend fun getAllLivesWithHistory(): List<LiveHistoryRelation>
}