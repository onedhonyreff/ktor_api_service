package id.bts.entities

import org.ktorm.schema.*

open class RoleEntity(alias: String?) : Table<Nothing>("roles", alias) {
  override fun aliased(alias: String) = RoleEntity(alias)

  val id = int("id").primaryKey()
  val name = varchar("name")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : RoleEntity(null) {
    const val HCM_ROLE_ALIAS = "hcm_role_alias"
  }
}