package id.bts.model.response.user

import id.bts.entities.SuperVisorEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class SuperVisor(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  val user: User?,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): SuperVisor {
      return SuperVisor(
        id = queryRowSet[SuperVisorEntity.id].toString(),
        userId = queryRowSet[SuperVisorEntity.userId].toString(),
        user = User.transform(queryRowSet),
        createdAt = queryRowSet[SuperVisorEntity.createdAt].toString(),
        updatedAt = queryRowSet[SuperVisorEntity.updatedAt].toString(),
        deletedAt = queryRowSet[SuperVisorEntity.deletedAt].toString()
      )
    }
  }
}