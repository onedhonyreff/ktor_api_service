package id.bts.entities

import org.ktorm.schema.*

open class LeaveApprovalEntity(alias: String?) : Table<Nothing>("leave_approvals", alias) {
  override fun aliased(alias: String) = LeaveApprovalEntity(alias)

  val id = int("id").primaryKey()
  val submittedLeaveRequestId = int("leave_request_id")
  val assignedUserId = int("assigned_user_id")
  val annotation = text("annotation")
  val allowedStartDate = date("allowed_start_date")
  val allowedEndDate = date("allowed_end_date")
  val allowedDuration = int("allowed_duration")
  val status = varchar("status")
  val approvedDate = timestamp("approved_date")
  val isCanceled = boolean("is_canceled")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")

  companion object : LeaveApprovalEntity(null) {
    const val APPROVAL_SOURCE = "approval_source"
  }
}