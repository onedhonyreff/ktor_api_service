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
    fun transform(queryRowSet: QueryRowSet, divisionEntity: DivisionEntity = DivisionEntity): Division? {
      return queryRowSet[divisionEntity.id]?.toString()?.let {
        Division(
          id = it,
          name = queryRowSet[divisionEntity.name],
          createdAt = queryRowSet[divisionEntity.createdAt].toString(),
          updatedAt = queryRowSet[divisionEntity.updatedAt].toString(),
          deletedAt = queryRowSet[divisionEntity.deletedAt].toString()
        )
      }
    }
  }
}
