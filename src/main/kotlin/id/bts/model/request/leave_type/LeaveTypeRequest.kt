package id.bts.model.request.leave_type

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaveTypeRequest(
  val name: String,
  val description: String? = null,
  @SerialName("default_duration")
  val defaultDuration: Int? = null
)
