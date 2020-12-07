package xandeer.android.synclip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.children
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setCallbacks()
    observe()
    openFromThird()
    initDarkMode()
    initRecentHosts()
  }

  private fun initDarkMode() {
    if (vm.isDarkMode) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
  }

  private val vm: ClipboardViewModel by viewModel()
  private fun setCallbacks() {
    host.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        val t = host.text?.toString()
        if (!t.isNullOrEmpty()) {
          vm.setHost(t)
        }
      }
    }
    port.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        port.text?.toString()?.toInt()?.let {
          vm.setPort(it)
        }
      }
    }
    port.setOnEditorActionListener { v, id, _ ->
      if (id == EditorInfo.IME_ACTION_DONE) {
        v.clearFocus()
      }
      false
    }

    buttons.children.forEach { v ->
      v.setOnClickListener { _ ->
        when (v.id) {
          R.id.fetch -> vm.fetch(true)
          R.id.toggle_dark_mode -> toggleDarkMode()
        }
        hideSoftInput()
      }
    }

    remote.setOnClickListener {
      val r = remote.text.toString()
      updateClipboard(r)
      vm.send(r)
    }
  }

  private fun toggleDarkMode() {
    val mode = if (vm.isDarkMode) {
      AppCompatDelegate.MODE_NIGHT_NO
    } else {
      AppCompatDelegate.MODE_NIGHT_YES
    }
    vm.setDarkMode(!vm.isDarkMode)
    AppCompatDelegate.setDefaultNightMode(mode)
  }

  private fun updateClipboard(text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
      ClipData.newPlainText(this.packageName, text)
    )
    local.text = text
    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
  }

  private fun hideSoftInput() {
    currentFocus?.let {
      it.clearFocus()
      (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.hideSoftInputFromWindow(it.applicationWindowToken, 0)
    }
  }

  private fun getClipboardText() =
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
      ?.getItemAt(0)
      ?.text?.toString()

  private fun observe() {
    vm.host.observe {
      host.setText(it)
      if (it.isNotEmpty()) {
        sync()
      }
    }

    vm.port.observe {
      port.setText(it.toString())
      sync()
    }

    vm.fetched.observe {
      remote.text = it
      if (vm.needUpdateClipboardAfterFetched) {
        updateClipboard(it)
      }
      vm.afterFetched()
    }

    vm.err.observe {
      if (it.isNotEmpty()) {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
      }
    }

    vm.recentHosts.observe {
      recentHosts.adapter = RecentHostsAdapter(it)
      Timber.i("Recent hosts: it")
    }
  }

  private fun initRecentHosts() {
    recentHosts.layoutManager = GridLayoutManager(this, 3)
  }

  override fun onResume() {
    super.onResume()
    sync()
  }

  private fun sync() {
    local.post {
      val l = getClipboardText()
      l?.let { local.text = it }
      vm.sync(l)
    }
  }

  private fun openFromThird() {
    val t = when (intent.action) {
      Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
      Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
      else -> null
    }

    t?.let {
      Timber.i("Shared data: $t")
      vm.send(it)
    }
  }

  private fun <T> LiveData<T>.observe(cb: ((it: T) -> Unit)) {
    observe(this@MainActivity, Observer {
      cb(it)
    })
  }
}