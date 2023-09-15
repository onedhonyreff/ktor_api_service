package id.bts.entities

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.timestamp

open class SuperVisorEntity(alias: String?) : Table<Nothing>("super_visors", alias) {
  override fun aliased(alias: String) = SuperVisorEntity(alias)

  val id = int("id").primaryKey()
  val userId = int("user_id")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : SuperVisorEntity(null) {
    const val HCM_SV_RELATION_ALIAS = "hcm_sv_relation_alias"
  }
}