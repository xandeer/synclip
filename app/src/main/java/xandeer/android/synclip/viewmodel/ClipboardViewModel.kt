package xandeer.android.synclip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
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

  private val _recentHosts = MutableLiveData<Set<String>>(repo.getRecentHosts())
  val recentHosts get() = _recentHosts as LiveData<Set<String>>

  private var isUpdatedHost = false
  fun sync(local: String?) {
    cancelable?.cancel()
    cancelable = viewModelScope.launch {
      try {
        repo.fetch()
          .collect {
            needUpdateClipboardAfterFetched = false
            _fetched.value = it.content
            Timber.d("Sync Fetched: $it")
          }

        if (_fetched.value != local && local != null) {
          repo.send(local)
            .collect {
              Timber.d("Sync Sent: $it")
            }
        }
        if (isUpdatedHost) {
          isUpdatedHost = false
          repo.setRecentHosts(_host.value!!)
          _recentHosts.value = repo.getRecentHosts()
        }
      } catch (e: CancellationException) {
      } catch (e: Throwable) {
        Timber.e(e, "Sync failed.")
        _err.value = e.message ?: ""
      }
    }
  }

  private var cancelable: Job? = null
  fun fetch(needUpdateAfterFetched: Boolean) {
    cancelable?.cancel()
    cancelable = viewModelScope.launch {
      try {
        repo.fetch()
          .collect {
            needUpdateClipboardAfterFetched = needUpdateAfterFetched
            _fetched.value = it.content
            Timber.d("Fetched: $it")
          }
      } catch (e: CancellationException) {
      } catch (e: Throwable) {
        Timber.e(e, "Fetch failed.")
        _err.value = e.message ?: ""
      }
    }
  }

  var needUpdateClipboardAfterFetched = false
    private set

  fun afterFetched() {
    needUpdateClipboardAfterFetched = false
  }

  fun send(text: String) {
    viewModelScope.launch {
      try {
        repo.send(text)
          .collect {
            _fetched.value = it.content
            Timber.d("Sent: $it")
          }
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
    isUpdatedHost = true
    repo.setHost(host)
    _host.value = host
  }

  fun setPort(port: Int) {
    repo.setPort(port)
    _port.value = port
  }

  val isDarkModeAtInit get() = repo.isDarkMode()
  private val _isDarkMode = MutableLiveData<Boolean>(isDarkModeAtInit)
  val isDarkMode get() = _isDarkMode as LiveData<Boolean>

  fun toggleDarkMode() {
    val next = !repo.isDarkMode()
    repo.setDarkMode(next)
    _isDarkMode.postValue(next)
  }
}