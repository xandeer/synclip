package xandeer.android.synclip.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import xandeer.android.synclip.databinding.ActivityMainBinding
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initDarkMode()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setCallbacks()
    observe()
    openFromThird()
    initRecentHosts()
  }

  private fun initDarkMode() {
    if (vm.isDarkModeAtInit) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
  }

  private val vm: ClipboardViewModel by viewModel()
  private fun setCallbacks() {
    binding.apply {
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

      buttons.setOnClickListener {
        hideSoftInput()
      }

      remote.setOnClickListener {
        val r = remote.text.toString()
        updateClipboard(r)
        vm.send(r)
      }
    }
  }

  private fun updateClipboard(text: String) {
    (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
      ClipData.newPlainText(this.packageName, text)
    )
    binding.local.text = text
    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
  }

  private fun hideSoftInput() {
    currentFocus?.let {
      it.clearFocus()
      (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.hideSoftInputFromWindow(it.applicationWindowToken, 0)
    }
  }

  private fun getClipboardText() =
    (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
      ?.getItemAt(0)
      ?.text?.toString()

  private fun observe() {
    vm.host.observe {
      binding.host.setText(it)
      if (it.isNotEmpty()) {
        sync()
      }
    }

    vm.port.observe {
      binding.port.setText(it.toString())
      sync()
    }

    vm.fetched.observe {
      binding.remote.text = it
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
      binding.recentHosts.adapter = RecentHostsAdapter(it)
      Timber.i("Recent hosts: it")
    }

    vm.isDarkMode.observe {
      val next = if (it) {
        AppCompatDelegate.MODE_NIGHT_YES
      } else {
        AppCompatDelegate.MODE_NIGHT_NO
      }
      AppCompatDelegate.setDefaultNightMode(next)
    }
  }

  private fun initRecentHosts() {
    binding.recentHosts.layoutManager = GridLayoutManager(this, 3)
  }

  override fun onResume() {
    super.onResume()
    sync()
  }

  private fun sync() {
    binding.local.post {
      val l = getClipboardText()
      l?.let { binding.local.text = it }
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