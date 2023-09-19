package id.bts.model.response.leave_request

import id.bts.entities.SubmittedLeaveRequestEntity
import id.bts.model.response.leave_approval.LeaveApproval
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.on_going_project.OnGoingProject
import id.bts.model.response.user.User
import id.bts.utils.DateTimeUtils.parseToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class SubmittedLeaveRequest(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  val requester: User? = null,
  @SerialName("leave_type_id")
  val leaveTypeId: String?,
  @SerialName("leave_type")
  val leaveType: LeaveType? = null,
  @SerialName("doctor_note")
  val doctorNote: String? = null,
  @SerialName("start_date")
  val startDate: String?,
  @SerialName("end_date")
  val endDate: String?,
  @SerialName("total_leave")
  val totalLeave: Int?,
  val reason: String?,
  @SerialName("first_super_visor_id")
  val firstSuperVisorId: String?,
  @SerialName("second_super_visor_id")
  val secondSuperVisorId: String?,
  val status: String?,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?,
  /* Part of Ongoing project */
  @SerialName("on_going_projects")
  val onGoingProjects: List<OnGoingProject>? = null, // detail
  /* Part of Leave Approval */
  @SerialName("leave_approvals")
  val leaveApprovals: List<LeaveApproval>? = null, // detail
  @SerialName("allowed_leave_duration")
  val allowedLeaveDuration: Int? = null
) {
  companion object {
    fun transform(
      queryRowSet: QueryRowSet,
      onGoingProjects: List<OnGoingProject>? = null,
      leaveApprovals: List<LeaveApproval>? = null,
      hideLeaveApprovals: Boolean = false
    ): SubmittedLeaveRequest {

      val allowedLeaveDuration = leaveApprovals?.let {
        var current: Int? = null
        it.forEach { leaveApproval ->
          if (current == null) {
            current = leaveApproval.allowedDuration
          } else if (current!! > leaveApproval.allowedDuration!!) {
            current = leaveApproval.allowedDuration
          }
        }
        current
      }

      return SubmittedLeaveRequest(
        id = queryRowSet[SubmittedLeaveRequestEntity.id].toString(),
        userId = queryRowSet[SubmittedLeaveRequestEntity.userId].toString(),
        requester = User.transform(queryRowSet, includeHcm = false),
        leaveTypeId = queryRowSet[SubmittedLeaveRequestEntity.leaveTypeId].toString(),
        leaveType = LeaveType.transform(queryRowSet),
        doctorNote = queryRowSet[SubmittedLeaveRequestEntity.doctorNote],
        startDate = queryRowSet[SubmittedLeaveRequestEntity.startDate].parseToString(),
        endDate = queryRowSet[SubmittedLeaveRequestEntity.endDate].parseToString(),
        totalLeave = queryRowSet[SubmittedLeaveRequestEntity.totalLeave],
        reason = queryRowSet[SubmittedLeaveRequestEntity.reason],
        firstSuperVisorId = queryRowSet[SubmittedLeaveRequestEntity.firstSuperVisorId].toString(),
        secondSuperVisorId = queryRowSet[SubmittedLeaveRequestEntity.secondSuperVisorId].toString(),
        status = queryRowSet[SubmittedLeaveRequestEntity.status],
        onGoingProjects = onGoingProjects,
        leaveApprovals = if (!hideLeaveApprovals) leaveApprovals else null,
        createdAt = queryRowSet[SubmittedLeaveRequestEntity.createdAt].toString(),
        updatedAt = queryRowSet[SubmittedLeaveRequestEntity.updatedAt].toString(),
        deletedAt = queryRowSet[SubmittedLeaveRequestEntity.deletedAt].toString(),
        allowedLeaveDuration = allowedLeaveDuration
      )
    }
  }
}
