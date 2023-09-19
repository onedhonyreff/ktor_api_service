package id.bts.model.response.leave_approval

import id.bts.entities.LeaveApprovalEntity
import id.bts.entities.UserEntity
import id.bts.model.response.user.User
import id.bts.utils.DateTimeUtils.parseToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class LeaveApproval(
  val id: String?,
  @SerialName("submitted_leave_request_id")
  val submittedLeaveRequestId: String?,
  @SerialName("assigned_user_id")
  val assignedUserId: String?,
  @SerialName("assigned_user")
  val assignedUser: User? = null,
  val annotation: String?,
  @SerialName("allowed_start_date")
  val allowedStartDate: String?,
  @SerialName("allowed_end_date")
  val allowedEndDate: String?,
  @SerialName("allowed_duration")
  val allowedDuration: Int?,
  val status: String?,
  @SerialName("approved_date")
  val approvedDate: String?,
  @SerialName("is_canceled")
  val isCanceled: Boolean? = false
) {
  companion object {
    fun transform(
      queryRowSet: QueryRowSet,
      includeUser: Boolean = true,
      userEntity: UserEntity = UserEntity,
    ): LeaveApproval? {
      return queryRowSet[LeaveApprovalEntity.id]?.toString()?.let {
        LeaveApproval(
          id = it,
          submittedLeaveRequestId = queryRowSet[LeaveApprovalEntity.submittedLeaveRequestId].toString(),
          assignedUserId = queryRowSet[LeaveApprovalEntity.assignedUserId].toString(),
          assignedUser = if (includeUser) User.transform(
            queryRowSet,
            userEntity = userEntity,
            includeHcm = false
          ) else null,
          annotation = queryRowSet[LeaveApprovalEntity.annotation],
          allowedStartDate = queryRowSet[LeaveApprovalEntity.allowedStartDate].parseToString(),
          allowedEndDate = queryRowSet[LeaveApprovalEntity.allowedEndDate].parseToString(),
          allowedDuration = queryRowSet[LeaveApprovalEntity.allowedDuration],
          status = queryRowSet[LeaveApprovalEntity.status].toString(),
          approvedDate = queryRowSet[LeaveApprovalEntity.approvedDate].toString(),
          isCanceled = queryRowSet[LeaveApprovalEntity.isCanceled]
        )
      }
    }
  }
}
