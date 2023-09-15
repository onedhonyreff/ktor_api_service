package id.bts.entities

import org.ktorm.schema.*

object OnGoingProjectEntity : Table<Nothing>("on_going_projects") {
  val id = int("id").primaryKey()
  val leaveRequestId = int("leave_request_id")
  val name = varchar("name")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}