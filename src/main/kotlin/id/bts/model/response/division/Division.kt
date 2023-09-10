package id.bts.model.response.division

import id.bts.entities.DivisionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Division(
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
    fun transform(queryRowSet: QueryRowSet): Division {
      return Division(
        id = queryRowSet[DivisionEntity.id].toString(),
        name = queryRowSet[DivisionEntity.name],
        createdAt = queryRowSet[DivisionEntity.createdAt].toString(),
        updatedAt = queryRowSet[DivisionEntity.updatedAt].toString(),
        deletedAt = queryRowSet[DivisionEntity.deletedAt].toString()
      )
    }
  }
}
