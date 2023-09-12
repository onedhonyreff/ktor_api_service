package id.bts.model.request.leave_allowance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaveAllowanceRequest(
  @SerialName("user_id")
  val userId: Int?,
  @SerialName("leave_type_id")
  val leaveTypeId: Int?,
  val duration: Int?
)