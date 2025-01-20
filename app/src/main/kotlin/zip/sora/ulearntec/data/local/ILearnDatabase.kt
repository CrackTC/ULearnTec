package zip.sora.ulearntec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import zip.sora.ulearntec.data.local.dao.ClassDao
import zip.sora.ulearntec.data.local.dao.LiveDao
import zip.sora.ulearntec.data.local.dao.LiveResourcesDao
import zip.sora.ulearntec.data.local.dao.TermDao
import zip.sora.ulearntec.data.local.dao.UserDao
import zip.sora.ulearntec.data.local.entity.ClassEntity
import zip.sora.ulearntec.data.local.entity.LiveEntity
import zip.sora.ulearntec.data.local.entity.LiveHistoryEntity
import zip.sora.ulearntec.data.local.entity.LiveResourcesEntity
import zip.sora.ulearntec.data.local.entity.TermEntity
import zip.sora.ulearntec.data.local.entity.UserEntity
import zip.sora.ulearntec.data.local.entity.VideoEntity

@Database(
    entities = [
        UserEntity::class,
        TermEntity::class,
        ClassEntity::class,
        LiveEntity::class,
        LiveResourcesEntity::class,
        LiveHistoryEntity::class,
        VideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ILearnDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val termDao: TermDao
    abstract val classDao: ClassDao
    abstract val liveDao: LiveDao
    abstract val liveResourcesDao: LiveResourcesDao
}