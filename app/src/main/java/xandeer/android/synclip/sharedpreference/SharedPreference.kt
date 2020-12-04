package xandeer.android.synclip.sharedpreference

import android.content.SharedPreferences
import androidx.core.content.edit
import timber.log.Timber

class SharedPreference(private val sp: SharedPreferences) {
  fun getHost(): String = sp.getString(HOST, "") ?: ""

  fun setHost(host: String) {
    sp.edit(true) { putString(HOST, host) }
    Timber.d("SP set host: $host")
  }

  fun getPort(): Int = sp.getInt(PORT, 3000)

  fun setPort(port: Int) {
    sp.edit(true) { putInt(PORT, port) }
    Timber.d("SP set port: $port")
  }

  fun isDarkMode(): Boolean = sp.getBoolean(IS_DARK_MODE, false)

  fun setDarkMode(enabled: Boolean) {
    sp.edit(true) { putBoolean(IS_DARK_MODE, enabled) }
    Timber.i("SP set is dark mode enabled: $enabled")
  }

  companion object {
    private const val HOST = "host"
    private const val PORT = "port"
    private const val IS_DARK_MODE = "is_dark_mode"
  }
}
