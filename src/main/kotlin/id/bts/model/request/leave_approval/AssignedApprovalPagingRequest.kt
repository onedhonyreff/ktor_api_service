package id.bts.model.request.leave_approval

import id.bts.model.request.PagingRequest
import id.bts.utils.DataConstants
import kotlinx.serialization.Serializable

@Serializable
data class AssignedApprovalPagingRequest(
  val status: List<String> = listOf(
    DataConstants.LeaveApprovalStatus.PENDING,
    DataConstants.LeaveApprovalStatus.APPROVED,
    DataConstants.LeaveApprovalStatus.REJECTED,
    DataConstants.LeaveApprovalStatus.CANCELED,
  )
) : PagingRequest()
