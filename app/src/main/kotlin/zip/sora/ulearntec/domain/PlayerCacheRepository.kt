package zip.sora.ulearntec.domain

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource

@OptIn(UnstableApi::class)
interface PlayerCacheRepository {
    suspend fun getCacheFactory(context: Context): DataSource.Factory
}