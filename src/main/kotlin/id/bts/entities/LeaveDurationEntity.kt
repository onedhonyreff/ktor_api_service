package id.bts.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp

object LeaveDurationEntity : Table<Nothing>("leave_durations") {
  val id = int("id").primaryKey()
  val userId = int("user_id")
  val leaveTypeId = int("leave_type_id")
  val duration = int("duration")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}