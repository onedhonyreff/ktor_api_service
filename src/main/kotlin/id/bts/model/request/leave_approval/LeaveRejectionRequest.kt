package id.bts.model.request.leave_approval

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaveRejectionRequest(
  @SerialName("approval_id")
  val approvalId: Int,
  val annotation: String
)
