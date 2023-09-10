package id.bts.model.response.leave_type

import id.bts.entities.LeaveTypeEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class LeaveType(
  val id: String?,
  @SerialName("initial_name")
  val initialName: String?,
  val name: String?,
  val description: String?,
  val illustration: String?,
  @SerialName("default_duration")
  val defaultDuration: Int?,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): LeaveType {
      return LeaveType(
        id = queryRowSet[LeaveTypeEntity.id].toString(),
        initialName = queryRowSet[LeaveTypeEntity.initial_name],
        name = queryRowSet[LeaveTypeEntity.name],
        description = queryRowSet[LeaveTypeEntity.description],
        defaultDuration = queryRowSet[LeaveTypeEntity.defaultDuration],
        illustration = queryRowSet[LeaveTypeEntity.illustration],
        createdAt = queryRowSet[LeaveTypeEntity.createdAt].toString(),
        updatedAt = queryRowSet[LeaveTypeEntity.updatedAt].toString(),
        deletedAt = queryRowSet[LeaveTypeEntity.deletedAt].toString()
      )
    }
  }
}
