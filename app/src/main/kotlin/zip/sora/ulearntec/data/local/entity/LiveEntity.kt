package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek

enum class LiveEntityStatus {
    PENDING,
    IN_PROGRESS,
    PULLING,
    FINISHED,
    NO_RECORDS,
}

@Entity(
    tableName = "live",
    foreignKeys = [
        ForeignKey(
            ClassEntity::class,
            ["id"],
            ["class_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("resource_id")]
)
data class LiveEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "resource_id") val resourceId: String?,
    @ColumnInfo(name = "live_record_name") val liveRecordName: String,
    @ColumnInfo(name = "building_name") val buildingName: String?,
    @ColumnInfo(name = "week") val week: Int,
    @ColumnInfo(name = "weekday") val weekday: DayOfWeek,
    @ColumnInfo(name = "room_name") val roomName: String?,
    @ColumnInfo(name = "room_id") val roomId: String?,
    @ColumnInfo(name = "teacher_name") val teacherName: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "course_name") val courseName: String,
    @ColumnInfo(name = "class_id", index = true) val classId: String,
    @ColumnInfo(name = "class_names") val classNames: String,
    @ColumnInfo(name = "section") val section: String,
    @ColumnInfo(name = "time_range") val timeRange: String,
    @ColumnInfo(name = "has_permission") val hasPermission: Boolean?,
    @ColumnInfo(name = "is_released") val isReleased: Boolean?,
    @ColumnInfo(name = "is_action") val isAction: Boolean?,
    @ColumnInfo(name = "live_status") val liveStatus: LiveEntityStatus,
    @ColumnInfo(name = "video_times") val videoTimes: Int?,
    @ColumnInfo(name = "class_type") val classType: String,
    @ColumnInfo(name = "room_type") val roomType: Int?,
    @ColumnInfo(name = "schedule_time_start") val scheduleTimeStart: Long,
    @ColumnInfo(name = "schedule_time_end") val scheduleTimeEnd: Long,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)