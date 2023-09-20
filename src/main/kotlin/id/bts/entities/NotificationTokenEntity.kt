package id.bts.entities

import org.ktorm.schema.*

object NotificationTokenEntity : Table<Nothing>("notification_tokens") {
  val id = int("id").primaryKey()
  val userId = int("user_id")
  val token = text("token")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}