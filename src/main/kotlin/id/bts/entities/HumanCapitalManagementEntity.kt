package id.bts.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp

object HumanCapitalManagementEntity : Table<Nothing>("human_capital_managements") {
  val id = int("id").primaryKey()
  val userId = int("user_id")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}