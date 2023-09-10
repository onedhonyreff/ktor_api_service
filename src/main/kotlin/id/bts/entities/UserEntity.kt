package id.bts.entities

import org.ktorm.schema.*

object UserEntity : Table<Nothing>("users") {
  val id = int("id").primaryKey()
  val email = varchar("email")
  val password = varchar("password")
  val name = varchar("name")
  val photo = varchar("photo")
  val nip = varchar("nip")
  val roleId = int("role_id")
  val divisionId = int("division_id")
  val departmentId = int("department_id")
  val humanCapitalManagementId = int("human_capital_management_id")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}