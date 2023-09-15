package id.bts.entities

import org.ktorm.schema.*

object LeaveApprovalEntity : Table<Nothing>("leave_approvals") {
  val id = int("id").primaryKey()
  val leaveRequestId = int("leave_request_id")
  val assignedUserId = int("assigned_user_id")
  val annotation = text("annotation")
  val allowedStartDate = date("allowed_start_date")
  val allowedEndDate = date("allowed_end_date")
  val allowedDuration = int("allowed_duration")
  val status = varchar("status")
  val approved_date = timestamp("approved_date")
  val is_canceled = boolean("is_canceled")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}