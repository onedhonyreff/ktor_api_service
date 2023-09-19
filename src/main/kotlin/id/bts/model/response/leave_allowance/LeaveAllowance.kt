package id.bts.model.response.leave_allowance

import id.bts.entities.LeaveAllowanceEntity
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.user.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class LeaveAllowance(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  @SerialName("leave_type_id")
  val leaveTypeId: String?,
  val user: User? = null,
  @SerialName("leave_type")
  val leaveType: LeaveType? = null,
  val duration: Int?,
  @SerialName("used_duration")
  val usedDuration: Int? = null
) {
  companion object {
    fun transform(
      queryRowSet: QueryRowSet,
      includeUser: Boolean = true,
      usedDuration: Int? = null
    ): LeaveAllowance {
      return LeaveAllowance(
        id = queryRowSet[LeaveAllowanceEntity.id].toString(),
        userId = queryRowSet[LeaveAllowanceEntity.userId].toString(),
        leaveTypeId = queryRowSet[LeaveAllowanceEntity.leaveTypeId].toString(),
        user = if (includeUser) User.transform(queryRowSet) else null,
        leaveType = LeaveType.transform(queryRowSet),
        duration = queryRowSet[LeaveAllowanceEntity.duration],
        usedDuration = usedDuration
      )
    }
  }
}
