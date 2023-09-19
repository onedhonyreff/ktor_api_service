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
  val note: String?,
  val illustration: String?,
  @SerialName("default_duration")
  val defaultDuration: Int?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): LeaveType? {
      return queryRowSet[LeaveTypeEntity.id]?.toString()?.let {
        LeaveType(
          id = it,
          initialName = queryRowSet[LeaveTypeEntity.initial_name],
          name = queryRowSet[LeaveTypeEntity.name],
          description = queryRowSet[LeaveTypeEntity.description],
          defaultDuration = queryRowSet[LeaveTypeEntity.defaultDuration],
          note = queryRowSet[LeaveTypeEntity.note],
          illustration = queryRowSet[LeaveTypeEntity.illustration]
        )
      }
    }
  }
}
