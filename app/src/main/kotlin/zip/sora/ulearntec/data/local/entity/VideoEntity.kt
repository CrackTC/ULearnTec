package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "video",
    foreignKeys = [
        ForeignKey(
            LiveResourcesEntity::class,
            ["resource_id"],
            ["resource_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class VideoEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "video_code") val videoCode: String,
    @ColumnInfo(name = "video_name") val videoName: String,
    @ColumnInfo(name = "video_path") val videoPath: String,
    @ColumnInfo(name = "video_size") val videoSize: String?,
    @ColumnInfo(name = "resource_id", index = true) val resourceId: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)
