package xandeer.android.synclip.network

import androidx.annotation.Keep
import xandeer.android.synclip.domain.DomainClipboard

@Keep
data class NetworkClipboard(
  val content: String,
  val err: String? = null
)

fun NetworkClipboard.asDomain() = DomainClipboard(content)