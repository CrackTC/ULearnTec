package zip.sora.ulearntec.ui

import zip.sora.ulearntec.domain.model.Class
import zip.sora.ulearntec.domain.model.ClassStatus
import zip.sora.ulearntec.domain.model.Live
import zip.sora.ulearntec.domain.model.LiveHistory
import zip.sora.ulearntec.domain.model.LiveStatus
import zip.sora.ulearntec.domain.model.Term
import zip.sora.ulearntec.domain.model.User
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

val mockUser = User(
    studentId = 123456,
    msg = "Good afternoon",
    studentNo = "21221234",
    studentName = "Lai Jinmei",
    schoolName = "Jilin University",
    userName = "jlu21220123",
    avatar = "https://ilearn.jlu.edu.cn/iplat/static/icon/1.jpg",
    memberId = "a1b2c3d4e5f6",
    lastUpdated = 0
)

val mockTerms = listOf(
    Term(
        year = 2022,
        num = 1,
        name = "First Semester",
        id = "46b13fe1f278425d84d5c05265443c89",
        startDate = "2022-08-01",
        endDate = "2023-01-31",
        lastUpdated = Instant.now().toEpochMilli()
    ),
    Term(
        year = 2022,
        num = 2,
        name = "Second Semester",
        id = "84d5c05265443c9046b13fe1f278425d",
        startDate = "2023-02-01",
        endDate = "2023-07-31",
        lastUpdated = Instant.now().toEpochMilli()
    )
)

val mockClasses = listOf(
    Class(
        id = "4100000000000097213", // Incremented ID
        name = "Introduction to Programming",
        courseId = UUID.randomUUID().toString().replace("-", "")
            .substring(0, 32), // Random courseId
        courseName = "Computer Science 101",
        cover = "https://ilearn.jlu.edu.cn/iplat/upload/course//202308/88fce899-d49c-4b97-b924-a094b406f199.jpg",
        teacherId = "24296", // Incremented teacherId
        teacherName = "Alice Johnson",
        status = ClassStatus.PENDING,
        teacherUsername = "alicej",
        schoolId = "54", // Incremented schoolId
        schoolName = "University A",
        year = 2022,
        num = 1,
        lastUpdated = 1700000000000
    ),
    Class(
        id = "4100000000000097214",
        name = "Calculus I",
        courseId = UUID.randomUUID().toString().replace("-", "")
            .substring(0, 32),
        courseName = "Mathematics 101",
        cover = "https://ilearn.jlu.edu.cn/iplat/upload/course/courseDefault.jpg",
        teacherId = "24297",
        teacherName = "Bob Williams",
        status = ClassStatus.FINISHED,
        teacherUsername = "bobw",
        schoolId = "55",
        schoolName = "University A",
        year = 2022,
        num = 2,
        lastUpdated = 1670000000000
    ),
    Class(
        id = "4100000000000097215",
        name = "Physics II",
        courseId = UUID.randomUUID().toString().replace("-", "")
            .substring(0, 32),
        courseName = "Physics 201",
        cover = "https://ilearn.jlu.edu.cn/iplat/upload/course/courseDefault.jpg",
        teacherId = "24298",
        teacherName = "Carol Davis",
        status = ClassStatus.IN_PROGRESS,
        teacherUsername = "carold",
        schoolId = "56",
        schoolName = "University B",
        year = 2022,
        num = 1,
        lastUpdated = 1710000000000
    ),
    Class(
        id = "4100000000000097216",
        name = "Chemistry Lab",
        courseId = UUID.randomUUID().toString().replace("-", "")
            .substring(0, 32),
        courseName = "Chemistry 102 Lab",
        cover = "https://ilearn.jlu.edu.cn/iplat/upload/course/courseDefault.jpg",
        teacherId = "24299",
        teacherName = "David Garcia",
        status = ClassStatus.IN_PROGRESS,
        teacherUsername = "davidg",
        schoolId = "57",
        schoolName = "University B",
        year = 2022,
        num = 2,
        lastUpdated = 1720000000000
    ),
    Class(
        id = "4100000000000097217",
        name = "English Literature",
        courseId = UUID.randomUUID().toString().replace("-", "")
            .substring(0, 32),
        courseName = "English 202",
        cover = "https://ilearn.jlu.edu.cn/iplat/upload/course/courseDefault.jpg",
        teacherId = "24300",
        teacherName = "Eve Rodriguez",
        status = ClassStatus.FINISHED,
        teacherUsername = "ever",
        schoolId = "58",
        schoolName = "Community College C",
        year = 2022,
        num = 1,
        lastUpdated = 1680000000000
    )
)

val mockLives = listOf(
    Live(
        id = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        resourceId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        liveRecordName = "离散数学Ⅱ_录播课_2022-12-28 08:00-09:40",
        week = 18,
        weekday = DayOfWeek.SATURDAY,
        buildingName = "前卫-逸夫楼",
        roomId = "49",
        roomName = "前卫-逸夫楼-第十阶梯",
        roomType = 1,
        teacherName = "濑津美",
        courseId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        courseName = "离散数学Ⅱ",
        classId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        classNames = "离散数学Ⅱ",
        classType = "4",
        section = "1,2",
        timeRange = "08:00-09:40",
        hasPermission = true,
        isReleased = true,
        isAction = false,
        liveStatus = LiveStatus.NO_RECORDS,
        videoTimes = 6239,
        history = LiveHistory("", Instant.now().toEpochMilli(), 3000000),
        scheduleTimeStart = LocalDateTime.of(2022, 12, 28, 8, 0, 0)
            .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
        scheduleTimeEnd = LocalDateTime.of(2022, 12, 28, 9, 40, 0)
            .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
        lastUpdated = Instant.now().toEpochMilli()
    ),
    Live(
        id = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        resourceId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        liveRecordName = "离散数学Ⅱ_录播课_2022-12-27 08:00-09:40",
        week = 18,
        weekday = DayOfWeek.FRIDAY,
        buildingName = "前卫-经信教学楼",
        roomId = "76",
        roomName = "前卫-经信教学楼-F区第二阶梯",
        roomType = 1,
        teacherName = "濑津美",
        courseId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        courseName = "离散数学Ⅱ",
        classId = UUID.randomUUID().toString().replace("-", "").substring(0, 32),
        classNames = "离散数学Ⅱ",
        classType = "4",
        section = "1,2",
        timeRange = "08:00-09:40",
        hasPermission = true,
        isReleased = true,
        isAction = false,
        liveStatus = LiveStatus.FINISHED,
        videoTimes = 6236,
        history = LiveHistory("", Instant.now().toEpochMilli(), 1000000),
        scheduleTimeStart = LocalDateTime.of(2022, 12, 27, 8, 0, 0)
            .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
        scheduleTimeEnd = LocalDateTime.of(2022, 12, 27, 9, 40, 0)
            .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli(),
        lastUpdated = Instant.now().toEpochMilli()
    )
)