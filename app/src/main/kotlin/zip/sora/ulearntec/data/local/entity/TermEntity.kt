package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "term",
    primaryKeys = ["year", "num"],
    foreignKeys = [
        ForeignKey(
            UserEntity::class,
            ["id"],
            ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class TermEntity(
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "num") val num: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)