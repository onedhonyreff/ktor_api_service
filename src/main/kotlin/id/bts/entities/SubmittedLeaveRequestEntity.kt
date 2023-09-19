package id.bts.entities

import org.ktorm.schema.*

object SubmittedLeaveRequestEntity : Table<Nothing>("leave_requests") {
  val id = int("id").primaryKey()
  val userId = int("user_id")
  val leaveTypeId = int("leave_type_id")
  val doctorNote = varchar("doctor_note")
  val startDate = date("start_date")
  val endDate = date("end_date")
  val totalLeave = int("total_leave")
  val reason = text("reason")
  val firstSuperVisorId = int("first_super_visor_id")
  val secondSuperVisorId = int("second_super_visor_id")
  val status = varchar("status")
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")
  val deletedAt = timestamp("deleted_at")
  val deletedFlag = boolean("deleted_flag")
}