package zip.sora.ulearntec.domain.model

data class LiveResources(
    val resourceId: String,
    val liveId: String,
    val phaseUrl: String,
    val audioPath: String,
    val videoList: List<Video>,
    val lastUpdated: Long
)

data class Video(
    val id: String,
    val videoCode: String,
    val videoName: String,
    val videoPath: String,
    val videoSize: String,
    val resourceId: String,
    val lastUpdated: Long
)

