package zip.sora.ulearntec.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import zip.sora.ulearntec.data.local.entity.LiveResourcesEntity
import zip.sora.ulearntec.data.local.entity.VideoEntity

@Dao
interface LiveResourcesDao {
    @Upsert
    suspend fun upsertLiveResources(resources: LiveResourcesEntity)

    @Upsert
    suspend fun upsertVideos(videos: List<VideoEntity>)

    @Query("SELECT * FROM live_resources WHERE resource_id = :resourceId")
    suspend fun getLiveResources(resourceId: String): LiveResourcesEntity?

    @Query("SELECT * FROM video WHERE resource_id = :resourceId")
    suspend fun getVideos(resourceId: String): List<VideoEntity>

    @Query("DELETE FROM video WHERE id = :id")
    suspend fun deleteVideo(id: String)

    @Transaction
    suspend fun refresh(resources: LiveResourcesEntity, videos: List<VideoEntity>) {
        val old = getVideos(resources.resourceId).map { it.id }.toSet()
        val new = videos.map { it.id }.toSet()

        upsertLiveResources(resources)
        (old - new).forEach { deleteVideo(it) }
        upsertVideos(videos)
    }
}