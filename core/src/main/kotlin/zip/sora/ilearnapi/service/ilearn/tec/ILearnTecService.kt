package zip.sora.ilearnapi.service.ilearn.tec

import kotlinx.serialization.Serializable
import zip.sora.ilearnapi.auth.ILearnCas
import zip.sora.ilearnapi.service.ilearn.ILearnService

class ILearnTecService internal constructor(
    cas: ILearnCas,
    onSessionFetched: (String) -> Unit,
) : ILearnService(
    "https://ilearntec.jlu.edu.cn",
    "/coursecenter/main/index",
    cas,
    "SESSION",
    onSessionFetched
) {
    @Serializable
    data class DataList<T>(val dataList: List<T>)

    @Serializable
    data class UserDto(
        val studentId: String,
        val msg: String,
        val studyNo: String,
        val studentName: String,
        val schoolName: String,
        val userName: String,
        val headPic: String,
        val memberId: String
    )

    @Serializable
    data class TermDto(
        val year: String,
        val endDate: String,
        val num: String,
        val name: String,
        val id: String,
        val startDate: String,
    )

    @Serializable
    data class ClassDto(
        val id: String,
        val name: String,
        val courseId: String,
        val courseName: String,
        val cover: String,
        val teacherId: String,
        val teacherName: String,
        val status: String,
        val statusName: String,
        val classId: String,
        val classroomId: String,
        val teacherUsername: String,
        val schoolId: String,
        val type: String,
        val typeName: String,
        val schoolName: String
    )

    @Serializable
    data class VideoClassDto(
        val videoClassId: String,
        val videoName: String
    )

    @Serializable
    data class LiveDto(
        val id: String,
        val resourceId: String?,
        val liveRecordName: String,
        val buildingName: String?, // might be null for outdoor classes
        val roomName: String?,
        val roomId: String?,
        val roomType: String?, // 1 smart, 2 normal
        val currentWeek: String,
        val currentDay: String,
        val currentDate: String,
        val teacherName: String,
        val courseId: String,
        val courseName: String,
        val classIds: String,
        val classNames: String,
        val section: String,
        val timeRange: String,
        val isNowPlay: String?,
        val isOpen: String?,
        val isAction: String?,
        val liveStatus: String,
        val schImgUrl: String,
        val videoTimes: String,
        val classType: String,
        val videoPath: String,
        val videoClassMap: List<VideoClassDto>?,
        val livePath: String, // [{hasVideo: number, id: string, rtmpUrl: "rtmp://...", videoCode: "1" | "2" | "3", videoName: "...", videoPath: "https://jwcilesson.jlu.edu.cn/live/de6bedacf20b0db7058dc3cab587dfd4.flv"}]
        val scheduleTimeStart: String,
        val scheduleTimeEnd: String
    )

    suspend fun getSelf() =
        get<UserDto>("/studycenter/platform/public/getUserInfo")

    suspend fun getTerms() =
        get<DataList<TermDto>>("/studycenter/platform/common/termList").dataList

    suspend fun getTermClasses(year: Int, num: Int) =
        get<DataList<ClassDto>>(
            "/studycenter/platform/classroom/myClassroom?termYear=$year&term=$num"
        ).dataList

    suspend fun getClassLives(classId: String) =
        get<DataList<LiveDto>>(
            "/coursecenter/liveAndRecord/getLiveAndRecordInfoList?roomType=0&identity=0&teachClassId=$classId"
        ).dataList
}

internal fun ILearnCas.authenticateILearnTec(
    onSessionFetched: (String) -> Unit
) = ILearnTecService(this, onSessionFetched)