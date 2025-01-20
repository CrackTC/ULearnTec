package zip.sora.ulearntec.domain.model

data class User(
    val studentId: Int,
    val msg: String,
    val studentNo: String,
    val studentName: String,
    val schoolName: String,
    val userName: String,
    val avatar: String,
    val memberId: String,
    val lastUpdated: Long
)

