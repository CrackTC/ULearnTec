package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "live_resources",
    foreignKeys = [
        ForeignKey(
            LiveEntity::class,
            ["id"],
            ["live_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class LiveResourcesEntity(
    @PrimaryKey @ColumnInfo(name = "resource_id") val resourceId: String,
    @ColumnInfo(name = "live_id", index = true) val liveId: String,
    @ColumnInfo(name = "phase_url") val phaseUrl: String,
    @ColumnInfo(name = "audio_path") val audioPath: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)