package zip.sora.ulearntec.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import zip.sora.ulearntec.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("DELETE FROM user")
    suspend fun clear()

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM user")
    suspend fun getCurrentUser(): UserEntity?
}