package id.bts.route

import id.bts.notification_messaging.PushNotification
import id.bts.database.DBConnection
import id.bts.entities.*
import id.bts.model.request.leave_approval.AssignedApprovalPagingRequest
import id.bts.model.request.leave_approval.LeaveAcceptanceRequest
import id.bts.model.request.leave_approval.LeaveRejectionRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_approval.AssignedApproval
import id.bts.model.response.leave_approval.LeaveApproval
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.notification.Notification
import id.bts.model.response.notification.NotificationToken
import id.bts.model.response.on_going_project.OnGoingProject
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.DataConstants
import id.bts.utils.DateTimeUtils.parseToDate
import id.bts.utils.DateTimeUtils.parseToLocalDate
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.Instant
import java.time.LocalDate

fun Application.configureLeaveApprovalRoute() {
  routing {
    authenticate("approver-authorization") {
      post("/my-assigned-approvals") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        val pagingRequest = call.receivePagingRequest<AssignedApprovalPagingRequest>()
        DBConnection.database?.let { database ->
          val assignedApprovals = database.from(LeaveApprovalEntity)
            .leftJoin(
              SubmittedLeaveRequestEntity,
              on = SubmittedLeaveRequestEntity.id eq LeaveApprovalEntity.submittedLeaveRequestId
            )
            .leftJoin(UserEntity, on = UserEntity.id eq SubmittedLeaveRequestEntity.userId)
            .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
            .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              (LeaveApprovalEntity.assignedUserId eq userId) and (SubmittedLeaveRequestEntity.status neq DataConstants.SubmittedLeaveRequestStatus.CANCELED) and (LeaveApprovalEntity.status inList pagingRequest.status) and (LeaveApprovalEntity.deletedFlag neq true)
            }.orderBy(LeaveApprovalEntity.id.desc()).map { AssignedApproval.transform(it) }
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = assignedApprovals,
              totalData = assignedApprovals.size
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      get("approval/detail/{approvalId}") {
        val approvalId = try {
          call.parameters["approvalId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        DBConnection.database?.let { database ->
          database.from(LeaveApprovalEntity)
            .leftJoin(
              SubmittedLeaveRequestEntity,
              on = SubmittedLeaveRequestEntity.id eq LeaveApprovalEntity.submittedLeaveRequestId
            )
            .leftJoin(UserEntity, on = UserEntity.id eq SubmittedLeaveRequestEntity.userId)
            .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
            .leftJoin(
              HumanCapitalManagementEntity,
              on = HumanCapitalManagementEntity.id eq UserEntity.humanCapitalManagementId
            )
            .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
            .select()
            .where {
              (LeaveApprovalEntity.id eq approvalId) and (LeaveApprovalEntity.deletedFlag neq true)
            }.map {
              val submittedLeaveRequestId = it[SubmittedLeaveRequestEntity.id]!!
              val onGoingProjects = database.from(OnGoingProjectEntity)
                .select().where {
                  (OnGoingProjectEntity.leaveRequestId eq submittedLeaveRequestId) and (OnGoingProjectEntity.deletedFlag neq true)
                }.orderBy(OnGoingProjectEntity.id.desc()).map { onGoingProjectRowSet ->
                  OnGoingProject.transform(onGoingProjectRowSet)
                }

              val approverIds = listOf(
                it[SubmittedLeaveRequestEntity.firstSuperVisorId],
                it[SubmittedLeaveRequestEntity.secondSuperVisorId],
                it[HumanCapitalManagementEntity.userId],
              )
              val leaveApprovals = mutableListOf<LeaveApproval>()
              approverIds.forEach { approverId ->
                approverId?.let { id ->
                  database.from(LeaveApprovalEntity)
                    .leftJoin(UserEntity, on = UserEntity.id eq LeaveApprovalEntity.assignedUserId)
                    .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
                    .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
                    .leftJoin(HumanCapitalManagementEntity, on = HumanCapitalManagementEntity.userId eq UserEntity.id)
                    .select().where {
                      (LeaveApprovalEntity.assignedUserId eq id) and (LeaveApprovalEntity.deletedFlag neq true)
                    }.map { leaveApprovalRowSet ->
                      LeaveApproval.transform(leaveApprovalRowSet)
                    }.firstOrNull()?.let { data -> leaveApprovals.add(data) }
                }
              }

              AssignedApproval.transform(
                it,
                includeDetail = true,
                onGoingProjects = onGoingProjects,
                leaveApprovals = leaveApprovals
              )
            }.firstOrNull()?.let { data ->
              val message = "Single data fetched successfully"
              call.respond(BaseResponse(success = true, message = message, data = data))
            } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("approval/approve") {
        val leaveAcceptanceRequest: LeaveAcceptanceRequest
        val allowedStartDate: LocalDate
        val allowedEndDate: LocalDate

        try {
          leaveAcceptanceRequest = call.receive<LeaveAcceptanceRequest>()
          allowedStartDate = leaveAcceptanceRequest.allowedStartDate.parseToDate().parseToLocalDate()!!
          allowedEndDate = leaveAcceptanceRequest.allowedEndDate.parseToDate().parseToLocalDate()!!
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val currentTime = Instant.now()
          val affected = database.update(LeaveApprovalEntity) {
            set(it.status, DataConstants.LeaveApprovalStatus.APPROVED)
            set(it.annotation, leaveAcceptanceRequest.annotation)
            set(it.allowedStartDate, allowedStartDate)
            set(it.allowedEndDate, allowedEndDate)
            set(it.allowedDuration, leaveAcceptanceRequest.allowedDuration)
            set(it.approvedDate, currentTime)
            set(it.updatedAt, currentTime)
            where {
              (it.id eq leaveAcceptanceRequest.approvalId) and (it.deletedFlag neq true)
            }
          }

          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val leaveApprovalSourceEntity = LeaveApprovalEntity.aliased(LeaveApprovalEntity.APPROVAL_SOURCE)
            val approvals = database.from(leaveApprovalSourceEntity)
              .leftJoin(
                LeaveApprovalEntity,
                on = LeaveApprovalEntity.submittedLeaveRequestId eq leaveApprovalSourceEntity.submittedLeaveRequestId
              )
              .select(LeaveApprovalEntity.columns).where {
                (leaveApprovalSourceEntity.id eq leaveAcceptanceRequest.approvalId) and (leaveApprovalSourceEntity.deletedFlag neq true)
              }.map { LeaveApproval.transform(it) }.filterNotNull()

            if (approvals.all { approval -> approval.status == DataConstants.LeaveApprovalStatus.APPROVED }) {
              val submittedLeaveRequestId = approvals[0].submittedLeaveRequestId!!.toInt()
              database.update(SubmittedLeaveRequestEntity) {
                set(it.status, DataConstants.SubmittedLeaveRequestStatus.APPROVED)
                set(it.updatedAt, Instant.now())
                where {
                  (it.id eq submittedLeaveRequestId) and (it.status neq DataConstants.SubmittedLeaveRequestStatus.CANCELED)
                }
              }

              // get leave type data
              var userId: Int? = null
              var requestedLeaveDuration: Int? = null
              val leaveType = database.from(SubmittedLeaveRequestEntity)
                .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
                .select().where {
                  (SubmittedLeaveRequestEntity.id eq submittedLeaveRequestId) and (SubmittedLeaveRequestEntity.deletedFlag neq true)
                }.map {
                  userId = it[SubmittedLeaveRequestEntity.userId]
                  requestedLeaveDuration = it[SubmittedLeaveRequestEntity.totalLeave]
                  LeaveType.transform(it)
                }.firstOrNull()

              // add notification data
              val pushNotificationTitle: String
              val approvedType = if (approvals.all { approval -> approval.allowedDuration == requestedLeaveDuration }) {
                pushNotificationTitle = DataConstants.NotificationTitle.LEAVE_APPROVAL_APPROVED
                "<b>Approved All</b> successfully!"
              } else {
                pushNotificationTitle = DataConstants.NotificationTitle.LEAVE_APPROVAL_APPROVED_PARTIALLY
                "<b>Approved Partially</b> successful!"
              }
              val notificationMessage = """
                Your <b>${leaveType?.name} Request</b> has been $approvedType
              """.trimIndent()
              val notificationData = Notification(
                firstRelationId = submittedLeaveRequestId.toString(),
                notificationType = DataConstants.NotificationType.LEAVE_APPROVAL,
                message = Regex("<.*?>").replace(notificationMessage, ""),
                tag = leaveType?.name
              )
              database.insert(NotificationEntity) {
                set(it.userId, userId)
                set(it.firstRelationId, notificationData.firstRelationId?.toInt())
                set(it.notificationType, notificationData.notificationType)
                set(it.message, notificationMessage)
                set(it.tag, notificationData.tag)
              }

              val notificationTokens = database.from(NotificationTokenEntity).select().where {
                (NotificationTokenEntity.userId eq (userId ?: -1)) and (NotificationTokenEntity.deletedFlag neq true)
              }.map { NotificationToken.transform(it).notificationToken }.filterNotNull()

              PushNotification.sendMessage(
                title = pushNotificationTitle,
                message = notificationData.message.toString(),
                tokenList = notificationTokens,
                data = notificationData
              )
            }

            val message = "Leave approval has been approved successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("approval/cancel/{approvalId}") {
        val approvalId = try {
          call.parameters["approvalId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val currentTime = Instant.now()
          val affected = database.update(LeaveApprovalEntity) {
            set(it.status, DataConstants.LeaveApprovalStatus.CANCELED)
            set(it.updatedAt, currentTime)
            where {
              (it.id eq approvalId) and (it.deletedFlag neq true)
            }
          }

          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val submittedLeaveRequestId = database.from(LeaveApprovalEntity).select().where {
              LeaveApprovalEntity.id eq approvalId
            }.map { LeaveApproval.transform(it) }.first()!!.submittedLeaveRequestId!!.toInt()

            database.update(SubmittedLeaveRequestEntity) {
              set(it.status, DataConstants.SubmittedLeaveRequestStatus.PENDING)
              set(it.updatedAt, Instant.now())
              where {
                (it.id eq submittedLeaveRequestId) and (it.status neq DataConstants.SubmittedLeaveRequestStatus.CANCELED)
              }
            }

            val message = "Leave approval has been canceled successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("approval/reject") {
        val leaveRejectionRequest = try {
          call.receive<LeaveRejectionRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val affected = database.update(LeaveApprovalEntity) {
            set(it.status, DataConstants.LeaveApprovalStatus.REJECTED)
            set(it.annotation, leaveRejectionRequest.annotation)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq leaveRejectionRequest.approvalId) and (it.deletedFlag neq true)
            }
          }

          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val submittedLeaveRequestId = database.from(LeaveApprovalEntity).select().where {
              LeaveApprovalEntity.id eq leaveRejectionRequest.approvalId
            }.map { LeaveApproval.transform(it) }.first()!!.submittedLeaveRequestId!!.toInt()

            database.update(SubmittedLeaveRequestEntity) {
              set(it.status, DataConstants.SubmittedLeaveRequestStatus.REJECTED)
              set(it.updatedAt, Instant.now())
              where {
                (it.id eq submittedLeaveRequestId) and (it.status neq DataConstants.SubmittedLeaveRequestStatus.CANCELED)
              }
            }

            // get leave type data
            var userId: Int? = null
            val leaveType = database.from(SubmittedLeaveRequestEntity)
              .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
              .select().where {
                (SubmittedLeaveRequestEntity.id eq submittedLeaveRequestId) and (SubmittedLeaveRequestEntity.deletedFlag neq true)
              }.map {
                userId = it[SubmittedLeaveRequestEntity.userId]
                LeaveType.transform(it)
              }.firstOrNull()

            // add notification
            val notificationMessage = """
                Your <b>${leaveType?.name} Request</b> has been <b>Rejected</b>
              """.trimIndent()
            val notificationData = Notification(
              firstRelationId = submittedLeaveRequestId.toString(),
              notificationType = DataConstants.NotificationType.LEAVE_APPROVAL,
              message = Regex("<.*?>").replace(notificationMessage, ""),
              tag = leaveType?.name
            )
            database.insert(NotificationEntity) {
              set(it.userId, userId)
              set(it.firstRelationId, notificationData.firstRelationId?.toInt())
              set(it.notificationType, notificationData.notificationType)
              set(it.message, notificationMessage)
              set(it.tag, notificationData.tag)
            }

            val notificationTokens = database.from(NotificationTokenEntity).select().where {
              (NotificationTokenEntity.userId eq (userId ?: -1)) and (NotificationTokenEntity.deletedFlag neq true)
            }.map { NotificationToken.transform(it).notificationToken }.filterNotNull()

            PushNotification.sendMessage(
              title = DataConstants.NotificationTitle.LEAVE_APPROVAL_REJECTED,
              message = notificationData.message.toString(),
              tokenList = notificationTokens,
              data = notificationData
            )

            val message = "Leave approval has been rejected successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}