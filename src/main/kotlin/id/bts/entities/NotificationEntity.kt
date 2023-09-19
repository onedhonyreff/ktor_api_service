package id.bts.entities

import org.ktorm.schema.*

object NotificationEntity : Table<Nothing>("notifications") {
  val id = int("id").primaryKey()
  val userId = int("user_id")
  val firstRelationId = int("first_relation_id")
  val notificationType = varchar("notification_type")
  val message = varchar("message")
  val tag = varchar("tag")
  val readed = boolean("readed")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}