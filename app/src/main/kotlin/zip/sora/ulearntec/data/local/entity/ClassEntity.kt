package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ClassEntityStatus {
    PENDING,
    IN_PROGRESS,
    FINISHED,
}

@Entity(
    tableName = "class",
    foreignKeys = [
        ForeignKey(
            TermEntity::class,
            ["year", "num"],
            ["year", "num"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("year", "num")
    ]
)
data class ClassEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "course_name") val courseName: String,
    @ColumnInfo(name = "cover") val cover: String,
    @ColumnInfo(name = "teacher_id") val teacherId: String,
    @ColumnInfo(name = "teacher_name") val teacherName: String,
    @ColumnInfo(name = "status") val status: ClassEntityStatus,
    @ColumnInfo(name = "teacher_username") val teacherUsername: String,
    @ColumnInfo(name = "school_id") val schoolId: String,
    @ColumnInfo(name = "school_name") val schoolName: String,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "num") val num: Int,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)