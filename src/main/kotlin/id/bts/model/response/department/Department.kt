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
    fun transform(queryRowSet: QueryRowSet, departmentEntity: DepartmentEntity = DepartmentEntity): Department? {
      return queryRowSet[departmentEntity.id]?.toString()?.let {
        Department(
          id = it,
          name = queryRowSet[departmentEntity.name],
          createdAt = queryRowSet[departmentEntity.createdAt].toString(),
          updatedAt = queryRowSet[departmentEntity.updatedAt].toString(),
          deletedAt = queryRowSet[departmentEntity.deletedAt].toString()
        )
      }
    }
  }
}
