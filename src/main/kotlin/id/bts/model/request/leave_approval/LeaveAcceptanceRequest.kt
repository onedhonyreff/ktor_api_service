package id.bts.model.request.leave_approval

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaveAcceptanceRequest(
  @SerialName("approval_id")
  val approvalId: Int,
  val annotation: String,
  @SerialName("allowed_start_date")
  val allowedStartDate: String,
  @SerialName("allowed_end_date")
  val allowedEndDate: String,
  @SerialName("allowed_duration")
  val allowedDuration: Int,
)
