package id.bts.entities

import org.ktorm.schema.*

open class UserEntity(alias: String?) : Table<Nothing>("users", alias) {
  override fun aliased(alias: String) = UserEntity(alias)

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

  companion object : UserEntity(null) {
    const val HCM_USER_ALIAS = "hcm_user_alias"
    const val FIRST_SV_USER_ALIAS = "first_sv_user_alias"
    const val SECOND_SV_USER_ALIAS = "second_sv_user_alias"
    const val APPROVER_USER_ALIAS = "approver_user_alias"
  }
}