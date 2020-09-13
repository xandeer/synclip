package xandeer.android.synclip.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import xandeer.android.synclip.sharedpreference.SharedPreference

object Retrofit {
  fun create(sp: SharedPreference): API {
    Timber.d("Server: ${sp.getHost()}:${sp.getPort()}")
    return getRetrofit("http://${sp.getHost()}:${sp.getPort()}")
      .create(API::class.java)
  }

  private fun getRetrofit(base: String): Retrofit =
    Retrofit.Builder()
      .baseUrl(base)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
}
