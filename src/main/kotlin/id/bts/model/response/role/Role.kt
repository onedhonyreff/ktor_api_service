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
    fun transform(queryRowSet: QueryRowSet, roleEntity: RoleEntity = RoleEntity): Role? {
      return queryRowSet[roleEntity.id]?.toString()?.let {
        Role(
          id = it,
          name = queryRowSet[roleEntity.name],
          createdAt = queryRowSet[roleEntity.createdAt].toString(),
          updatedAt = queryRowSet[roleEntity.updatedAt].toString(),
          deletedAt = queryRowSet[roleEntity.deletedAt].toString()
        )
      }
    }
  }
}
