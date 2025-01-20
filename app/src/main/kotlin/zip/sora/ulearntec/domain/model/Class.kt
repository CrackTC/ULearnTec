package zip.sora.ulearntec.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import zip.sora.ulearntec.R

enum class ClassStatus(
    @StringRes val description: Int,
    val imageVector: ImageVector
) {
    NONE(R.string.all, Icons.Filled.FilterList),
    PENDING(R.string.pending, Icons.Filled.Schedule),
    IN_PROGRESS(R.string.ongoing, Icons.AutoMirrored.Filled.DirectionsWalk),
    FINISHED(R.string.finished, Icons.Filled.DoneAll),
}

@Serializable
data class Class(
    val id: String,
    val name: String,
    val courseId: String,
    val courseName: String,
    val cover: String,
    val teacherId: String,
    val teacherName: String,
    val status: ClassStatus,
    val teacherUsername: String,
    val schoolId: String,
    val schoolName: String,
    val year: Int,
    val num: Int,
    val lastUpdated: Long
)

