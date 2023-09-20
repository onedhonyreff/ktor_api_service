package id.bts.model.request.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationTokenRequest(
  @SerialName("notification_token")
  val notificationToken: String
)
