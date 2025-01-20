package zip.sora.ulearntec.di

import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import zip.sora.ulearntec.data.ApiRepositoryImpl
import zip.sora.ulearntec.data.ClassRepositoryImpl
import zip.sora.ulearntec.data.LiveRepositoryImpl
import zip.sora.ulearntec.data.LiveResourcesRepositoryImpl
import zip.sora.ulearntec.data.PreferenceRepositoryImpl
import zip.sora.ulearntec.data.TermRepositoryImpl
import zip.sora.ulearntec.data.UserRepositoryImpl
import zip.sora.ulearntec.data.local.ILearnDatabase
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ClassRepository
import zip.sora.ulearntec.domain.LiveRepository
import zip.sora.ulearntec.domain.LiveResourcesRepository
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.TermRepository
import zip.sora.ulearntec.domain.UserRepository
import zip.sora.ulearntec.ui.screen.main.AccountViewModel
import zip.sora.ulearntec.ui.screen.LoginViewModel
import zip.sora.ulearntec.ui.screen.PlayerViewModel
import zip.sora.ulearntec.ui.screen.main.HistoryViewModel
import zip.sora.ulearntec.ui.screen.main.course.ClassViewModel
import zip.sora.ulearntec.ui.screen.main.course.TermViewModel

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

    singleOf(::PreferenceRepositoryImpl) bind PreferenceRepository::class
    singleOf(::ApiRepositoryImpl) bind ApiRepository::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
    singleOf(::TermRepositoryImpl) bind TermRepository::class
    singleOf(::ClassRepositoryImpl) bind ClassRepository::class
    singleOf(::LiveRepositoryImpl) bind LiveRepository::class
    singleOf(::LiveResourcesRepositoryImpl) bind LiveResourcesRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::TermViewModel)
    viewModelOf(::ClassViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::AccountViewModel)
    viewModelOf(::HistoryViewModel)
}