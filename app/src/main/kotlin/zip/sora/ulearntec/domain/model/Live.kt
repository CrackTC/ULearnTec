package zip.sora.ulearntec.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import zip.sora.ulearntec.R
import java.time.DayOfWeek

enum class LiveStatus(
    @StringRes val description: Int,
    val imageVector: ImageVector
) {
    NONE(R.string.all, Icons.Filled.FilterList),
    PENDING(R.string.pending, Icons.Filled.Schedule),
    IN_PROGRESS(R.string.ongoing, Icons.AutoMirrored.Filled.DirectionsWalk),
    PULLING(R.string.pulling, Icons.Filled.Sync),
    FINISHED(R.string.finished, Icons.Filled.DoneAll),
    NO_RECORDS(R.string.no_records, Icons.Filled.VideocamOff)
}

@Serializable
data class Live(
    val id: String,
    val resourceId: String?,
    val liveRecordName: String,
    val week: Int,
    val weekday: DayOfWeek,
    val buildingName: String?,
    val roomId: String?,
    val roomName: String?,
    val roomType: Int?,
    val teacherName: String,
    val courseId: String,
    val courseName: String,
    val classId: String,
    val classNames: String,
    val classType: String,
    val section: String,
    val timeRange: String,
    val hasPermission: Boolean?,
    val isReleased: Boolean?,
    val isAction: Boolean?,
    val liveStatus: LiveStatus,
    val history: LiveHistory?,
    val videoTimes: Int?,
    val scheduleTimeStart: Long,
    val scheduleTimeEnd: Long,
    val lastUpdated: Long
)

