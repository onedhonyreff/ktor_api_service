package id.bts.model.response.notification

import id.bts.entities.NotificationEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Notification(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  @SerialName("first_relation_id")
  val firstRelationId: String?,
  @SerialName("notification_type")
  val notificationType: String?,
  val message: String?,
  val tag: String?,
  val readed: Boolean?,
  @SerialName("created_at")
  val createdAt: String?,
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): Notification {
      return Notification(
        id = queryRowSet[NotificationEntity.id].toString(),
        userId = queryRowSet[NotificationEntity.userId].toString(),
        firstRelationId = queryRowSet[NotificationEntity.firstRelationId].toString(),
        notificationType = queryRowSet[NotificationEntity.notificationType],
        message = queryRowSet[NotificationEntity.message],
        tag = queryRowSet[NotificationEntity.tag],
        readed = queryRowSet[NotificationEntity.readed],
        createdAt = queryRowSet[NotificationEntity.createdAt].toString(),
      )
    }
  }
}
