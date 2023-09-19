package id.bts.model.response.role

import id.bts.entities.RoleEntity
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Role(
  val id: String?,
  val name: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet, roleEntity: RoleEntity = RoleEntity): Role? {
      return queryRowSet[roleEntity.id]?.toString()?.let {
        Role(
          id = it,
          name = queryRowSet[roleEntity.name]
        )
      }
    }
  }
}
