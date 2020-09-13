package xandeer.android.synclip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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
  }

  private val vm: ClipboardViewModel by viewModel()
  private fun setCallbacks() {
    host.setOnEditorActionListener { v, id, _ ->
      if (id == EditorInfo.IME_ACTION_NEXT) {
        val t = v.text?.toString()
        if (!t.isNullOrEmpty()) {
          vm.setHost(t)
        }
      }
      false
    }
    port.setOnEditorActionListener { v, id, _ ->
      if (id == EditorInfo.IME_ACTION_DONE) {
        v.text?.toString()?.toInt()?.let {
          vm.setPort(it)
        }
        v.clearFocus()
      }
      false
    }

    fetch.setOnClickListener {
      vm.fetch()
    }

    send.setOnClickListener {
      getClipboardText()?.let {
        vm.send(it)
      }
    }

    fetched.setOnClickListener {
      (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
        ClipData.newPlainText(this.packageName, fetched.text)
      )
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
        vm.fetch()
      }
    }

    vm.port.observe {
      port.setText(it.toString())
      vm.fetch()
    }

    vm.fetched.observe {
      fetched.text = it
    }

    vm.err.observe {
      if (it.isNotEmpty()) {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun openFromThird() {
    val t = when (intent.action) {
      Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
      Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
      ACTION_SYNCLIP_SHORTCUT -> {
        intent.getStringExtra(SHORTCUT_NAME)?.let {
          Timber.i("Handle shortcut: $it")
          fetched.post { // post for getting clipboard
            handleShortcuts(it)
          }
        }
        null
      }
      else -> null
    }

    t?.let {
      Timber.i("Shared data: $t")
      vm.send(it)
    }
  }

  private fun handleShortcuts(name: String) {
    when (name) {
      FETCH -> vm.fetch()
      SEND -> getClipboardText()?.let { vm.send(it) }
    }
  }

  private fun <T> LiveData<T>.observe(cb: ((it: T) -> Unit)) {
    observe(this@MainActivity, Observer {
      cb(it)
    })
  }

  companion object {
    private const val ACTION_SYNCLIP_SHORTCUT = "xandeer.android.synclip.shortcut"
    private const val SHORTCUT_NAME = "shortcut_name"
    private const val FETCH = "fetch"
    private const val SEND = "send"
  }
}