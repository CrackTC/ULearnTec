package zip.sora.ilearnapi.service.ilearn.res

import kotlinx.serialization.Serializable
import zip.sora.ilearnapi.auth.ILearnCas
import zip.sora.ilearnapi.service.ilearn.ILearnService

class ILearnResService internal constructor(
    cas: ILearnCas,
    onSessionFetched: (String) -> Unit
) : ILearnService(
    "https://ilearnres.jlu.edu.cn",
    "/resource-center/user/casLogin?client_name=resource-center",
    cas,
    "JSESSIONID",
    onSessionFetched
) {

    @Serializable
    data class VideoDto(
        val id: String,
        val videoCode: String,
        val videoName: String,
        val videoPath: String,
        val videoSize: String? = null
    )

    @Serializable
    data class LiveResourcesDto(
        val scheduleId: String,
        val videoList: List<VideoDto>,
        val phaseUrl: String,
        val audioPath: String
    )

    suspend fun getLiveVideos(resourceId: String) =
        get<LiveResourcesDto>("/resource-center/videoclass/videoClassInfo?resourceId=$resourceId")
}

internal fun ILearnCas.authenticateILearnRes(
    onSessionFetched: (String) -> Unit
) = ILearnResService(this, onSessionFetched)