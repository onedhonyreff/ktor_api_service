package id.bts.model.request.leave_request

import id.bts.model.request.PagingRequest
import id.bts.utils.DataConstants
import kotlinx.serialization.Serializable

@Serializable
data class SubmittedLeavePagingRequest(
  val status: List<String> = listOf(
    DataConstants.SubmittedLeaveRequestStatus.PENDING,
    DataConstants.SubmittedLeaveRequestStatus.APPROVED,
    DataConstants.SubmittedLeaveRequestStatus.REJECTED,
    DataConstants.SubmittedLeaveRequestStatus.CANCELED,
  )
) : PagingRequest()
