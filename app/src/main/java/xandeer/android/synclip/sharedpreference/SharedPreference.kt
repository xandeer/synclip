package xandeer.android.synclip.sharedpreference

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreference(private val sp: SharedPreferences) {
  fun getHost(): String = sp.getString(HOST, "") ?: ""

  fun setHost(host: String) {
    sp.edit { putString(HOST, host) }
  }

  fun getPort(): Int = sp.getInt(PORT, 3000)

  fun setPort(port: Int) {
    sp.edit { putInt(PORT, port) }
  }

  companion object {
    private const val HOST = "host"
    private const val PORT = "port"
  }
}