package xandeer.android.synclip

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.getViewModel
import xandeer.android.synclip.viewmodel.ClipboardViewModel

class RecentHostsAdapter(private val hosts: Set<String>) :
  RecyclerView.Adapter<RecentHostsAdapter.VH>() {

  inner class VH(private val button: MaterialButton) : RecyclerView.ViewHolder(button) {
    fun bind(host: String) {
      button.text = host
      button.setOnClickListener {
        (it.context as ComponentActivity).getViewModel<ClipboardViewModel>()
          .setHost(host)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val view = MaterialButton(parent.context).apply {
      layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    return VH(view)
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.bind(hosts.elementAt(position))
  }

  override fun getItemCount(): Int = hosts.size
}