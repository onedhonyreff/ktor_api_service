package id.bts.entities

import org.ktorm.schema.*

object DepartmentEntity: Table<Nothing>("departments") {
  val id = int("id").primaryKey()
  val name = varchar("name")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}