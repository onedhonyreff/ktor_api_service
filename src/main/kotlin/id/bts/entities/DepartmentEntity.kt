package id.bts.entities

import org.ktorm.schema.*

open class DepartmentEntity(alias: String?) : Table<Nothing>("departments", alias) {
  override fun aliased(alias: String) = DepartmentEntity(alias)

  val id = int("id").primaryKey()
  val name = varchar("name")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : DepartmentEntity(null) {
    const val HCM_DEPARTMENT_ALIAS = "hcm_department_alias"
  }
}