package zip.sora.ulearntec.data

import zip.sora.ilearnapi.service.ilearn.res.ILearnResService
import zip.sora.ilearnapi.service.ilearn.tec.ILearnTecService
import zip.sora.ulearntec.data.local.entity.ClassEntity
import zip.sora.ulearntec.data.local.entity.ClassEntityStatus
import zip.sora.ulearntec.data.local.entity.LiveEntity
import zip.sora.ulearntec.data.local.entity.LiveEntityStatus
import zip.sora.ulearntec.data.local.entity.LiveHistoryEntity
import zip.sora.ulearntec.data.local.entity.LiveResourcesEntity
import zip.sora.ulearntec.data.local.entity.TermEntity
import zip.sora.ulearntec.data.local.entity.UserEntity
import zip.sora.ulearntec.data.local.entity.VideoEntity
import zip.sora.ulearntec.data.local.relation.LiveHistoryRelation
import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.ClassStatus
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveHistory
import zip.sora.ulearntec.domain.model.LiveResources
import zip.sora.ulearntec.domain.model.LiveStatus
import zip.sora.ulearntec.domain.model.Term
import zip.sora.ulearntec.domain.model.User
import zip.sora.ulearntec.domain.model.Video
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun UserEntity.toUser() = User(
    studentId = studentId,
    msg = msg,
    studentNo = studentNo,
    studentName = studentName,
    schoolName = schoolName,
    userName = userName,
    avatar = avatar,
    memberId = memberId,
    lastUpdated = lastUpdated
)

fun ILearnTecService.UserDto.toUserEntity(lastUpdated: Long) = UserEntity(
    studentId = studentId.toInt(),
    msg = msg,
    studentNo = studyNo,
    studentName = studentName,
    schoolName = schoolName,
    userName = userName,
    avatar = headPic,
    memberId = memberId,
    lastUpdated = lastUpdated
)

fun TermEntity.toTerm() = Term(
    year = year,
    num = num,
    name = name,
    id = id,
    startDate = startDate,
    endDate = endDate,
    lastUpdated = lastUpdated
)

fun ILearnTecService.TermDto.toTermEntity(userId: Int, lastUpdated: Long) = TermEntity(
    year = year.toInt(),
    num = num.toInt(),
    userId = userId,
    name = name,
    id = id,
    startDate = startDate,
    endDate = endDate,
    lastUpdated = lastUpdated
)

fun ClassEntity.toClass() = Class(
    id = id,
    name = name,
    courseId = courseId,
    courseName = courseName,
    cover = cover,
    teacherId = teacherId,
    teacherName = teacherName,
    status = when (status) {
        ClassEntityStatus.PENDING -> ClassStatus.PENDING
        ClassEntityStatus.IN_PROGRESS -> ClassStatus.IN_PROGRESS
        ClassEntityStatus.FINISHED -> ClassStatus.FINISHED
    },
    teacherUsername = teacherUsername,
    schoolId = schoolId,
    schoolName = schoolName,
    year = year,
    num = num,
    lastUpdated = lastUpdated
)

fun ILearnTecService.ClassDto.toEntity(year: Int, num: Int, lastUpdated: Long) = ClassEntity(
    id = id,
    name = name,
    courseId = courseId,
    courseName = courseName,
    cover = cover,
    teacherId = teacherId,
    teacherName = teacherName,
    status = when (status) {
        "0" -> ClassEntityStatus.PENDING
        "1" -> ClassEntityStatus.IN_PROGRESS
        "2" -> ClassEntityStatus.FINISHED
        else -> throw IndexOutOfBoundsException("Status out of range: $status")
    },
    teacherUsername = teacherUsername,
    schoolId = schoolId,
    schoolName = schoolName,
    year = year,
    num = num,
    lastUpdated = lastUpdated
)

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val zoneId = ZoneId.of("Asia/Shanghai")
private fun parseDateTime(s: String) =
    LocalDateTime.parse(s, dateTimeFormatter).atZone(zoneId).toInstant().toEpochMilli()

fun ILearnTecService.LiveDto.toLiveEntity(classId: String, lastUpdated: Long) = LiveEntity(
    id = id,
    resourceId = resourceId,
    liveRecordName = liveRecordName,
    buildingName = buildingName,
    week = currentWeek.toInt(),
    weekday = DayOfWeek.of(currentDay.toInt()),
    roomName = roomName,
    roomId = roomId,
    teacherName = teacherName,
    courseId = courseId,
    courseName = courseName,
    classId = classId,
    classNames = classNames,
    section = section,
    timeRange = timeRange,
    hasPermission = isNowPlay.let { it == "1" },
    isReleased = isOpen?.let { it == "1" },
    isAction = isAction?.let { it == "1" },
    liveStatus = when (liveStatus) {
        "1" -> LiveEntityStatus.PENDING
        "2" -> LiveEntityStatus.IN_PROGRESS
        "3" -> LiveEntityStatus.FINISHED
        "4" -> LiveEntityStatus.NO_RECORDS
        "5" -> LiveEntityStatus.PULLING
        else -> throw IndexOutOfBoundsException("Status out of range: $liveStatus")
    },
    videoTimes = videoTimes.toInt(),
    classType = classType,
    roomType = roomType?.toInt(),
    scheduleTimeStart = parseDateTime(scheduleTimeStart),
    scheduleTimeEnd = parseDateTime(scheduleTimeEnd),
    lastUpdated = lastUpdated,
)

fun LiveHistoryRelation.toLive() = Live(
    id = live.id,
    resourceId = live.resourceId,
    liveRecordName = live.liveRecordName,
    week = live.week,
    weekday = live.weekday,
    buildingName = live.buildingName,
    roomId = live.roomId,
    roomName = live.roomName,
    roomType = live.roomType,
    teacherName = live.teacherName,
    courseId = live.courseId,
    courseName = live.courseName,
    classId = live.classId,
    classNames = live.classNames,
    classType = live.classType,
    section = live.section,
    timeRange = live.timeRange,
    hasPermission = live.hasPermission,
    isReleased = live.isReleased,
    isAction = live.isAction,
    liveStatus = when (live.liveStatus) {
        LiveEntityStatus.PENDING -> LiveStatus.PENDING
        LiveEntityStatus.IN_PROGRESS -> LiveStatus.IN_PROGRESS
        LiveEntityStatus.PULLING -> LiveStatus.PULLING
        LiveEntityStatus.FINISHED -> LiveStatus.FINISHED
        LiveEntityStatus.NO_RECORDS -> LiveStatus.NO_RECORDS
    },
    videoTimes = live.videoTimes,
    history = history?.let { LiveHistory(it.liveId, it.timestamp, it.positionMillis) },
    scheduleTimeStart = live.scheduleTimeStart,
    scheduleTimeEnd = live.scheduleTimeEnd,
    lastUpdated = live.lastUpdated,
)

fun ILearnResService.LiveResourcesDto.toLiveResourcesEntity(resourceId: String, lastUpdated: Long) =
    LiveResourcesEntity(
        resourceId = resourceId,
        liveId = scheduleId,
        phaseUrl = phaseUrl,
        audioPath = audioPath,
        lastUpdated = lastUpdated
    )

fun LiveResourcesEntity.toLiveResources(videoList: List<VideoEntity>) = LiveResources(
    resourceId = resourceId,
    liveId = liveId,
    phaseUrl = phaseUrl,
    audioPath = audioPath,
    videoList = videoList.map { it.toVideo() },
    lastUpdated = lastUpdated
)

fun ILearnResService.VideoDto.toVideoEntity(resourceId: String, lastUpdated: Long) = VideoEntity(
    id = id,
    videoCode = videoCode,
    videoName = videoName,
    videoPath = videoPath,
    videoSize = videoSize,
    resourceId = resourceId,
    lastUpdated = lastUpdated
)

fun VideoEntity.toVideo() = Video(
    id = id,
    videoCode = videoCode,
    videoName = videoName,
    videoPath = videoPath,
    videoSize = videoSize,
    resourceId = resourceId,
    lastUpdated = lastUpdated
)

fun LiveHistory.toLiveHistoryEntity() = LiveHistoryEntity(liveId, timestamp, positionMillis)