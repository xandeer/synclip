package xandeer.android.synclip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
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
          R.id.send -> getClipboardText()?.let {
            vm.send(it)
          }
        }
        hideSoftInput()
      }
    }

    fetched.setOnClickListener {
      updateClipboard(fetched.text.toString())
    }

    (buttons.parent as View).setOnClickListener {
      hideSoftInput()
    }
  }

  private fun updateClipboard(text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
      ClipData.newPlainText(this.packageName, text)
    )
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
        vm.fetch(false)
      }
    }

    vm.port.observe {
      port.setText(it.toString())
      vm.fetch(false)
    }

    vm.fetched.observe {
      fetched.text = it
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
      FETCH -> vm.fetch(true)
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