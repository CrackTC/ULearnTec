package zip.sora.ulearntec.di

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import zip.sora.ulearntec.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import zip.sora.ulearntec.data.ApiRepositoryImpl
import zip.sora.ulearntec.data.ClassRepositoryImpl
import zip.sora.ulearntec.data.DownloadRepositoryImpl
import zip.sora.ulearntec.data.LiveRepositoryImpl
import zip.sora.ulearntec.data.LiveResourcesRepositoryImpl
import zip.sora.ulearntec.data.PreferenceRepositoryImpl
import zip.sora.ulearntec.data.TermRepositoryImpl
import zip.sora.ulearntec.data.UserRepositoryImpl
import zip.sora.ulearntec.data.local.ILearnDatabase
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.TermRepository
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.ui.screen.LoginViewModel
import zip.sora.ulearntec.ui.screen.PlayerViewModel
import zip.sora.ulearntec.ui.screen.main.AccountViewModel
import zip.sora.ulearntec.ui.screen.main.DownloadViewModel
import zip.sora.ulearntec.ui.screen.main.HistoryViewModel
import zip.sora.ulearntec.ui.screen.main.course.ClassViewModel
import zip.sora.ulearntec.ui.screen.main.course.TermViewModel
import java.io.File
import java.util.concurrent.Executor

data object PlayerCacheKey
data object PlayerCacheFactoryKey

data object DownloadCacheKey
data object DownloadCacheFactoryKey

@SuppressLint("UnsafeOptInUsageError")
val defaultModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            ILearnDatabase::class.java,
            "ilearn_db.db"
        ).build()
    }

    single { get<ILearnDatabase>().userDao }
    single { get<ILearnDatabase>().termDao }
    single { get<ILearnDatabase>().classDao }
    single { get<ILearnDatabase>().liveDao }
    single { get<ILearnDatabase>().liveResourcesDao }

    singleOf(::StandaloneDatabaseProvider) bind DatabaseProvider::class

    single(named<PlayerCacheKey>()) {
        SimpleCache(
            File(androidApplication().externalCacheDir, "player"),
            LeastRecentlyUsedCacheEvictor(256 * 1024 * 1024),
            get()
        )
    } bind Cache::class

    single(named<PlayerCacheFactoryKey>()) {
        CacheDataSource.Factory()
            .setCache(get(named<PlayerCacheKey>()))
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
    } bind DataSource.Factory::class

    single(named<DownloadCacheKey>()) {
        SimpleCache(
            File(androidApplication().getExternalFilesDir(null), "download"),
            NoOpCacheEvictor(),
            get()
        )
    } bind Cache::class

    single {
        DownloadManager(
            androidApplication(),
            get(),
            get(named<DownloadCacheKey>()),
            DefaultHttpDataSource.Factory(),
            Executor(Runnable::run)
        )
    }

    single(named<DownloadCacheFactoryKey>()) {
        CacheDataSource.Factory()
            .setCache(get(named<DownloadCacheKey>()))
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheWriteDataSinkFactory(null)
    } bind DataSource.Factory::class

    single {
        val channel = NotificationChannel(
            DOWNLOAD_NOTIFICATION_CHANNEL_ID,
            DOWNLOAD_NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW
        )
        androidApplication().getSystemService<NotificationManager>()!!
            .createNotificationChannel(channel)
        DownloadNotificationHelper(androidApplication(), DOWNLOAD_NOTIFICATION_CHANNEL_ID)
    }

    singleOf(::PreferenceRepositoryImpl) bind PreferenceRepository::class
    singleOf(::ApiRepositoryImpl) bind ApiRepository::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
    singleOf(::TermRepositoryImpl) bind TermRepository::class
    singleOf(::ClassRepositoryImpl) bind ClassRepository::class
    singleOf(::LiveRepositoryImpl) bind LiveRepository::class
    singleOf(::LiveResourcesRepositoryImpl) bind LiveResourcesRepository::class
    singleOf(::DownloadRepositoryImpl) bind DownloadRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::TermViewModel)
    viewModelOf(::ClassViewModel)
    viewModel {
        PlayerViewModel(
            savedStateHandle = get(),
            cacheDataSourceFactory = get(named<PlayerCacheFactoryKey>()),
            downloadDataSourceFactory = get(named<DownloadCacheFactoryKey>()),
            liveRepository = get(),
            liveResourcesRepository = get(),
            downloadRepository = get()
        )
    }
    viewModelOf(::DownloadViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::AccountViewModel)
}