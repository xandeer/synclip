package xandeer.android.synclip.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get
import xandeer.android.synclip.network.API
import xandeer.android.synclip.network.NetworkClipboard
import xandeer.android.synclip.network.asDomain
import xandeer.android.synclip.sharedpreference.SharedPreference

class ClipboardRepository(
//  private val api: API,
  private val sp: SharedPreference
) {
  private val api get() = get(API::class.java)

  suspend fun fetch() = withContext(Dispatchers.IO) {
    val v = api.fetch()
    if (v.err.isNullOrEmpty()) {
      v.asDomain()
    } else {
      throw RuntimeException(v.err)
    }
  }

  suspend fun send(text: String) = withContext(Dispatchers.IO) {
    val v = api.send(NetworkClipboard(text))
    if (v.err.isNullOrEmpty()) {
      v.asDomain()
    } else {
      throw RuntimeException(v.err)
    }
  }

  fun getHost(): String = sp.getHost()

  fun setHost(host: String) {
    sp.setHost(host)
  }

  fun getPort(): Int = sp.getPort()

  fun setPort(port: Int) {
    sp.setPort(port)
  }
}