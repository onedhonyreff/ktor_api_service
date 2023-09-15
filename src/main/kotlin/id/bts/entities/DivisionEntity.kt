package id.bts.entities

import org.ktorm.schema.*

open class DivisionEntity(alias: String?) : Table<Nothing>("divisions", alias) {
  override fun aliased(alias: String) = DivisionEntity(alias)

  val id = int("id").primaryKey()
  val name = varchar("name")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : DivisionEntity(null) {
    const val HCM_DIVISION_ALIAS = "hcm_division_alias"
  }
}