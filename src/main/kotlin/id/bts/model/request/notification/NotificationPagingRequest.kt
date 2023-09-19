package id.bts.model.request.notification

import id.bts.model.request.PagingRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationPagingRequest(
  val readed: Boolean? = null,
  @SerialName("is_new")
  val isNew: Boolean? = null
) : PagingRequest()
