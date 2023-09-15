package id.bts.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp

open class HumanCapitalManagementEntity(alias: String?) : Table<Nothing>("human_capital_managements", alias) {
  override fun aliased(alias: String) = HumanCapitalManagementEntity(alias)

  val id = int("id").primaryKey()
  val userId = int("user_id")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : HumanCapitalManagementEntity(null) {
    const val USER_HCM_RELATION_ALIAS = "user_hcm_relation_alias"
    const val HCM_HCM_RELATION_ALIAS = "hcm_hcm_relation_alias"
  }
}