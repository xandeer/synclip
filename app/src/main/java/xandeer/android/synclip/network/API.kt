package xandeer.android.synclip.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface API {
  @GET("clipboard")
  suspend fun fetch(): NetworkClipboard

  @POST("clipboard")
  suspend fun send(
    @Body body: NetworkClipboard
  ): NetworkClipboard
}