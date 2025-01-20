package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "live_history",
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
data class LiveHistoryEntity(
    @PrimaryKey @ColumnInfo(name = "live_id") val liveId: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "position_millis") val positionMillis: Long
)