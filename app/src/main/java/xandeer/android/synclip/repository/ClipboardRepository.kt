package xandeer.android.synclip.repository

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.get
import xandeer.android.synclip.network.API
import xandeer.android.synclip.network.NetworkClipboard
import xandeer.android.synclip.network.asDomain
import xandeer.android.synclip.sharedpreference.SharedPreference

class ClipboardRepository(
  private val sp: SharedPreference
) {
  private val api get() = get(API::class.java)

  fun fetch() = flow {
    emit(api.fetch())
  }.map {
    if (it.err.isNullOrEmpty()) {
      it.asDomain()
    } else {
      throw RuntimeException(it.err)
    }
  }.flowOn(IO)

  fun send(text: String) = flow {
    emit(api.send(NetworkClipboard(text)))
  }.map {
    if (it.err.isNullOrEmpty()) {
      it.asDomain()
    } else {
      throw RuntimeException(it.err)
    }
  }.flowOn(IO)

  fun getHost(): String = sp.getHost()

  fun setHost(host: String) {
    sp.setHost(host)
  }

  fun getPort(): Int = sp.getPort()

  fun setPort(port: Int) {
    sp.setPort(port)
  }
}