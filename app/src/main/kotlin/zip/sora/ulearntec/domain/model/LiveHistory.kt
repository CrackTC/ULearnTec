package zip.sora.ulearntec.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LiveHistory(
    val liveId: String,
    val timestamp: Long,
    val positionMillis: Long,
)