package id.bts.model.response.leave_approval

import id.bts.entities.*
import id.bts.model.response.on_going_project.OnGoingProject
import id.bts.utils.DateTimeUtils.parseToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class AssignedApproval(
  /* Part of Leave Approval */
  val id: String?,
  val status: String?,
  /* Part of Submitted Leave Request */
  @SerialName("submitted_leave_request_start_date")
  val submittedLeaveRequestStartDate: String?,
  @SerialName("submitted_leave_request_end_date")
  val submittedLeaveRequestEndDate: String?,
  @SerialName("submitted_leave_request_total_duration")
  val submittedLeaveRequestTotalDuration: Int?,
  @SerialName("submitted_leave_request_created_at")
  val submittedLeaveRequestCreatedAt: String?,
  val reason: String? = null, // detail
  /* Part of User as Requester */
  @SerialName("requester_name")
  val requesterName: String?,
  @SerialName("requester_photo")
  val requesterPhoto: String?,
  /* Part of Role as Requester Role */
  @SerialName("requester_role")
  val requesterRole: String?,
  /* Part of Leave Type */
  @SerialName("leave_type_name")
  val leaveTypeName: String?,
  /* Part of Ongoing project */
  @SerialName("on_going_projects")
  val onGoingProjects: List<OnGoingProject>? = null, // detail
  /* Part of Leave Approval */
  @SerialName("leave_approvals")
  val leaveApprovals: List<LeaveApproval>? = null, // detail
) {
  companion object {
    fun transform(
      queryRowSet: QueryRowSet,
      includeDetail: Boolean = false,
      onGoingProjects: List<OnGoingProject>? = null,
      leaveApprovals: List<LeaveApproval>? = null
    ): AssignedApproval {
      return AssignedApproval(
        id = queryRowSet[LeaveApprovalEntity.id].toString(),
        status = queryRowSet[LeaveApprovalEntity.status],
        submittedLeaveRequestStartDate = queryRowSet[SubmittedLeaveRequestEntity.startDate].parseToString(),
        submittedLeaveRequestEndDate = queryRowSet[SubmittedLeaveRequestEntity.endDate].parseToString(),
        submittedLeaveRequestTotalDuration = queryRowSet[SubmittedLeaveRequestEntity.totalLeave],
        submittedLeaveRequestCreatedAt = queryRowSet[SubmittedLeaveRequestEntity.createdAt].toString(),
        reason = if (includeDetail) queryRowSet[SubmittedLeaveRequestEntity.reason] else null,
        requesterName = queryRowSet[UserEntity.name],
        requesterPhoto = queryRowSet[UserEntity.photo],
        requesterRole = queryRowSet[RoleEntity.name],
        leaveTypeName = queryRowSet[LeaveTypeEntity.name],
        onGoingProjects = if (includeDetail) onGoingProjects else null,
        leaveApprovals = if (includeDetail) leaveApprovals else null
      )
    }
  }
}
