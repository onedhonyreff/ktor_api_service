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
}