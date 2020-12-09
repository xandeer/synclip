package xandeer.android.synclip.ui

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.getViewModel
import xandeer.android.synclip.R
import xandeer.android.synclip.hideSoftInput
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class RecentHostsAdapter(private val hosts: Set<String>) :
  RecyclerView.Adapter<RecentHostsAdapter.VH>() {

  inner class VH(private val button: MaterialButton) : RecyclerView.ViewHolder(button) {
    fun bind(host: String) {
      button.text = host
      button.setOnClickListener {
        (it.context as ComponentActivity).getViewModel<ClipboardViewModel>()
          .setHost(host)
        it.hideSoftInput()
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val view = MaterialButton(parent.context, null, R.attr.minorButton).apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
    }

    return VH(view)
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.bind(hosts.elementAt(position))
  }

  override fun getItemCount(): Int = hosts.size
}