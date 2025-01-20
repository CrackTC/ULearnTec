package zip.sora.ulearntec.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = "id") val studentId: Int,
    @ColumnInfo(name = "msg") val msg: String,
    @ColumnInfo(name = "study_no") val studentNo: String,
    @ColumnInfo(name = "student_name") val studentName: String,
    @ColumnInfo(name = "school_name") val schoolName: String,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "head_pic") val avatar: String,
    @ColumnInfo(name = "member_id") val memberId: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)