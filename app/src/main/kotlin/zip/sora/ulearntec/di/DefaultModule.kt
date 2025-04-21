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
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import zip.sora.ulearntec.DATABASE_NAME
import zip.sora.ulearntec.DOWNLOAD_CACHE_DIR_NAME
import zip.sora.ulearntec.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import zip.sora.ulearntec.MainViewModel
import zip.sora.ulearntec.data.ApiRepositoryImpl
import zip.sora.ulearntec.data.ClassRepositoryImpl
import zip.sora.ulearntec.data.DownloadRepositoryImpl
import zip.sora.ulearntec.data.LiveRepositoryImpl
import zip.sora.ulearntec.data.LiveResourcesRepositoryImpl
import zip.sora.ulearntec.data.PlayerCacheRepositoryImpl
import zip.sora.ulearntec.data.PreferenceRepositoryImpl
import zip.sora.ulearntec.data.TermRepositoryImpl
import zip.sora.ulearntec.data.UserRepositoryImpl
import zip.sora.ulearntec.data.local.ILearnDatabase
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.DownloadRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.PlayerCacheRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.TermRepository
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.ui.screen.LoginViewModel
import zip.sora.ulearntec.ui.screen.PlayerViewModel
import zip.sora.ulearntec.ui.screen.main.MoreViewModel
import zip.sora.ulearntec.ui.screen.main.DownloadViewModel
import zip.sora.ulearntec.ui.screen.main.HistoryViewModel
import zip.sora.ulearntec.ui.screen.ClassViewModel
import zip.sora.ulearntec.ui.screen.main.TermViewModel
import zip.sora.ulearntec.ui.screen.SettingsViewModel
import java.io.File
import java.util.concurrent.Executor

@SuppressLint("UnsafeOptInUsageError")
val defaultModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            ILearnDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    single { get<ILearnDatabase>().userDao }
    single { get<ILearnDatabase>().termDao }
    single { get<ILearnDatabase>().classDao }
    single { get<ILearnDatabase>().liveDao }
    single { get<ILearnDatabase>().liveResourcesDao }

    singleOf(::StandaloneDatabaseProvider) bind DatabaseProvider::class

    single {
        SimpleCache(
            File(androidApplication().getExternalFilesDir(null), DOWNLOAD_CACHE_DIR_NAME),
            NoOpCacheEvictor(),
            get()
        )
    } bind Cache::class

    single {
        DownloadManager(
            androidApplication(),
            get(),
            get(),
            DefaultHttpDataSource.Factory(),
            Executor(Runnable::run)
        )
    }

    single {
        CacheDataSource.Factory()
            .setCache(get())
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
    singleOf(::PlayerCacheRepositoryImpl) bind PlayerCacheRepository::class

    viewModelOf(::MainViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::TermViewModel)
    viewModelOf(::ClassViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::DownloadViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::MoreViewModel)
    viewModelOf(::SettingsViewModel)
}