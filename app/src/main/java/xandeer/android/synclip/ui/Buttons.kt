package xandeer.android.synclip.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel
import xandeer.android.synclip.databinding.ButtonsBinding
import xandeer.android.synclip.layoutInflater
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class Buttons : FrameLayout {
  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  )

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context) : this(context, null)

  init {
    val binding = ButtonsBinding.inflate(layoutInflater, this, true)
    val vm = (context as ComponentActivity).getViewModel<ClipboardViewModel>()

    binding.fetch.setOnClickListener {
      vm.fetch(true)
    }
    binding.toggleDarkMode.setOnClickListener {
      vm.toggleDarkMode()
    }
  }
}