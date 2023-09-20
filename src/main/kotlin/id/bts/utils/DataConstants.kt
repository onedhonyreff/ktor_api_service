package id.bts.utils

object DataConstants {
  object FileAccessType {
    const val OPEN = "open"
    const val DOWNLOAD = "download"
  }

  object SubmittedLeaveRequestStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val CANCELED = "CANCELED"
  }

  object LeaveApprovalStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val CANCELED = "CANCELED"
  }

  object NotificationType {
    const val LEAVE_REQUEST = "LEAVE_REQUEST"
    const val LEAVE_APPROVAL = "LEAVE_APPROVAL"
  }

  object NotificationTitle {
    const val LEAVE_APPLICATION_SENT = "Leave Application Sent"
    const val LEAVE_APPROVAL_APPROVED = "Approved Leave"
    const val LEAVE_APPROVAL_APPROVED_PARTIALLY = "Approved Partially Leave"
    const val LEAVE_APPROVAL_REJECTED = "Rejected Leave"
  }
}