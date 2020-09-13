package xandeer.android.synclip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import xandeer.android.synclip.repository.ClipboardRepository

class ClipboardViewModel(
  private val repo: ClipboardRepository
) : ViewModel() {
  private val _fetched = MutableLiveData<String>("")

  val fetched get() = _fetched as LiveData<String>

  private val _err = MutableLiveData<String>("")

  val err get() = _err as LiveData<String>

  fun fetch() {
    viewModelScope.launch {
      try {
        val v = repo.fetch()
        _fetched.value = v.content
        Timber.d("Fetched: $v")
      } catch (e: Throwable) {
        Timber.e(e, "Fetch failed.")
        _err.value = e.message ?: ""
      }
    }
  }

  fun send(text: String) {
    viewModelScope.launch {
      try {
        val v= repo.send(text)
        _fetched.value = v.content
      } catch (e: Throwable) {
        Timber.e(e, "Send failed.")
        _err.value = e.message ?: ""
      }
    }
  }

  private val _host = MutableLiveData<String>(repo.getHost())
  private val _port = MutableLiveData<Int>(repo.getPort())

  val host get() = _host as LiveData<String>
  val port get() = _port as LiveData<Int>

  fun setHost(host: String) {
    repo.setHost(host)
    _host.value = host
  }

  fun setPort(port: Int) {
    repo.setPort(port)
    _port.value = port
  }
}