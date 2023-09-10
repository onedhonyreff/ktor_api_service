package id.bts.model.response.role

import id.bts.entities.RoleEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Role(
  val id: String?,
  val name: String?,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): Role {
      return Role(
        id = queryRowSet[RoleEntity.id].toString(),
        name = queryRowSet[RoleEntity.name],
        createdAt = queryRowSet[RoleEntity.createdAt].toString(),
        updatedAt = queryRowSet[RoleEntity.updatedAt].toString(),
        deletedAt = queryRowSet[RoleEntity.deletedAt].toString()
      )
    }
  }
}
