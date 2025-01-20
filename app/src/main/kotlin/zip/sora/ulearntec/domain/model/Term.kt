package zip.sora.ulearntec.domain.model

data class Term(
    val year: Int,
    val num: Int,
    val name: String,
    val id: String,
    val startDate: String,
    val endDate: String,
    val lastUpdated: Long
)

