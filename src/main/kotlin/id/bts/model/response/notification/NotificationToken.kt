package id.bts.model.response.notification

import id.bts.entities.NotificationTokenEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class NotificationToken(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  @SerialName("notification_token")
  val notificationToken: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): NotificationToken {
      return NotificationToken(
        id = queryRowSet[NotificationTokenEntity.id].toString(),
        userId = queryRowSet[NotificationTokenEntity.userId].toString(),
        notificationToken = queryRowSet[NotificationTokenEntity.token]
      )
    }
  }
}
