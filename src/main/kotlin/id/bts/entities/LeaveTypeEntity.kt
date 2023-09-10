package id.bts.entities

import org.ktorm.schema.*

object LeaveTypeEntity: Table<Nothing>("leave_types") {
  val id = int("id").primaryKey()
  val initial_name = varchar("initial_name")
  val name = varchar("name")
  val description = varchar("description")
  val defaultDuration = int("default_duration")
  val illustration = varchar("illustration")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}