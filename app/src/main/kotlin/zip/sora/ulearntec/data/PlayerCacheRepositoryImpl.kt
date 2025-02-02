package zip.sora.ulearntec.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import zip.sora.ulearntec.PLAYER_CACHE_DIR_NAME
import zip.sora.ulearntec.domain.PlayerCacheRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import java.io.File

@OptIn(UnstableApi::class)
class PlayerCacheRepositoryImpl(
    private val preferenceRepository: PreferenceRepository,
    private val databaseProvider: DatabaseProvider
): PlayerCacheRepository {
    private var currentMaxMb: Long? = null
    private var currentCache: Cache? = null
    override suspend fun getCacheFactory(context: Context): DataSource.Factory {
        val maxMb = preferenceRepository.getMaxPlayerCacheMb()
        if (maxMb != currentMaxMb) {
            currentMaxMb = maxMb
            currentCache?.release()
            currentCache = SimpleCache(
                File(context.externalCacheDir, PLAYER_CACHE_DIR_NAME),
                LeastRecentlyUsedCacheEvictor(maxMb * 1024 * 1024),
                databaseProvider
            )
        }
        return CacheDataSource.Factory()
            .setCache(currentCache!!)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
    }
}