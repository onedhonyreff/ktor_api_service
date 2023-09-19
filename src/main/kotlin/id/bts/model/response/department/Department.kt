package id.bts.model.response.department

import id.bts.entities.DepartmentEntity
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Department(
  val id: String?,
  val name: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet, departmentEntity: DepartmentEntity = DepartmentEntity): Department? {
      return queryRowSet[departmentEntity.id]?.toString()?.let {
        Department(
          id = it,
          name = queryRowSet[departmentEntity.name]
        )
      }
    }
  }
}
