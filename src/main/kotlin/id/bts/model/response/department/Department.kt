package id.bts.model.response.department

import id.bts.entities.DepartmentEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Department(
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
    fun transform(queryRowSet: QueryRowSet): Department {
      return Department(
        id = queryRowSet[DepartmentEntity.id].toString(),
        name = queryRowSet[DepartmentEntity.name],
        createdAt = queryRowSet[DepartmentEntity.createdAt].toString(),
        updatedAt = queryRowSet[DepartmentEntity.updatedAt].toString(),
        deletedAt = queryRowSet[DepartmentEntity.deletedAt].toString()
      )
    }
  }
}
