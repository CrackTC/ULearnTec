package zip.sora.ulearntec

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import zip.sora.ilearnapi.createILearnHttpClient
import zip.sora.ulearntec.di.defaultModule

class App : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(defaultModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context = context)
            .components { add(factory = KtorNetworkFetcherFactory(httpClient = createILearnHttpClient())) }
            .crossfade(true)
            .build()
    }
}