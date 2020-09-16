package xandeer.android.synclip

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import xandeer.android.synclip.network.Retrofit
import xandeer.android.synclip.repository.ClipboardRepository
import xandeer.android.synclip.sharedpreference.SharedPreference
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class App : Application() {
  private val appModule = module {
    single {
      SharedPreference(
        androidContext().getSharedPreferences("synclip", MODE_PRIVATE)
      )
    }

    factory { Retrofit.create(get()) }

    factory { ClipboardRepository(get()) }

    viewModel { ClipboardViewModel(get()) }
  }

  override fun onCreate() {
    super.onCreate()

    Timber.plant(object : Timber.DebugTree() {
      override fun createStackElementTag(element: StackTraceElement): String {
        return String.format("Xandeer:${super.createStackElementTag(element)}")
      }
    })

    startKoin {
      androidLogger()
      androidContext(this@App)
      modules(appModule)
    }
  }
}