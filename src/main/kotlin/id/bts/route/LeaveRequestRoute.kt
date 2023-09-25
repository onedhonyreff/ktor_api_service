package id.bts.route

import id.bts.notification_messaging.PushNotification
import id.bts.database.DBConnection
import id.bts.entities.*
import id.bts.model.request.leave_request.SubmittedLeavePagingRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_approval.LeaveApproval
import id.bts.model.response.leave_request.SubmittedLeaveRequest
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.notification.Notification
import id.bts.model.response.notification.NotificationToken
import id.bts.model.response.on_going_project.OnGoingProject
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.DataConstants
import id.bts.utils.DateTimeUtils.parseToDate
import id.bts.utils.DateTimeUtils.parseToLocalDate
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.replaceFileName
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import id.bts.utils.ProjectUtils
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.ktorm.dsl.*
import java.io.File
import java.time.LocalDate

fun Application.configureLeaveRequestRoute() {
  routing {
    authenticate("user-authorization") {
      post("/leave-request") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        val multipart = try {
          call.receiveMultipart()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        var leaveTypeId: Int? = null
        var doctorNote: String? = null
        var startDate: LocalDate? = null
        var endDate: LocalDate? = null
        var totalLeave: Int? = null
        var reason: String? = null
        var firstSuperVisorId: Int? = null
        var secondSuperVisorId: Int? = null
        var onGoingProjects: List<String>? = null
        val assignedUserIds = mutableListOf<Int>()

        try {
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FileItem -> {
                when (part.name) {
                  "doctor_note" -> {
                    val inputStream = part.streamProvider()
                    val filename = part.originalFileName.replaceFileName()

                    val directory = File("${ProjectUtils.getProjectDir()}/assets/images/doctor_note")
                    if (!directory.exists()) {
                      directory.mkdir()
                    }

                    val filePath = "$directory/$filename"
                    val destinationFile = File(filePath)
                    inputStream.copyTo(destinationFile.outputStream().buffered())
                    doctorNote = "/file/open/image/doctor_note/$filename"
                  }
                }
              }

              is PartData.FormItem -> {
                when (part.name) {
                  "leave_type_id" -> leaveTypeId = part.value.toInt()
                  "start_date" -> startDate = part.value.parseToDate().parseToLocalDate()
                  "end_date" -> endDate = part.value.parseToDate().parseToLocalDate()
                  "total_leave" -> totalLeave = part.value.toInt()
                  "reason" -> reason = part.value
                  "first_super_visor_id" -> {
                    firstSuperVisorId = part.value.toInt()
                    assignedUserIds.add(firstSuperVisorId!!)
                  }

                  "second_super_visor_id" -> {
                    secondSuperVisorId = part.value.toInt()
                    assignedUserIds.add(secondSuperVisorId!!)
                  }

                  "on_going_projects" -> onGoingProjects = Json.decodeFromString<List<String>>(part.value)
                }
              }

              else -> Unit
            }
          }

          DBConnection.database?.let { database ->
            database.from(UserEntity)
              .leftJoin(
                HumanCapitalManagementEntity,
                on = HumanCapitalManagementEntity.id eq UserEntity.humanCapitalManagementId
              )
              .select(HumanCapitalManagementEntity.userId)
              .where {
                (UserEntity.id eq userId) and (UserEntity.deletedFlag neq true)
              }.map { it[HumanCapitalManagementEntity.userId]?.toInt() }.firstOrNull()?.let { hcmId ->
                assignedUserIds.add(hcmId)
              }

            val leaveRequestId = database.insertAndGenerateKey(SubmittedLeaveRequestEntity) {
              set(it.userId, userId)
              set(it.leaveTypeId, leaveTypeId)
              set(it.startDate, startDate)
              set(it.endDate, endDate)
              set(it.doctorNote, doctorNote)
              set(it.totalLeave, totalLeave)
              set(it.reason, reason)
              set(it.firstSuperVisorId, firstSuperVisorId)
              set(it.secondSuperVisorId, secondSuperVisorId)
              set(it.status, DataConstants.SubmittedLeaveRequestStatus.PENDING)
            } as Int?

            if (leaveRequestId == 0) {
              call.returnNotImplementedResponse()
            } else {
              // add ongoing projects
              onGoingProjects?.forEach { onGoingProject ->
                database.insert(OnGoingProjectEntity) {
                  set(it.leaveRequestId, leaveRequestId)
                  set(it.name, onGoingProject)
                }
              }

              // add approvals
              assignedUserIds.forEach { assignedUserId ->
                database.insert(LeaveApprovalEntity) {
                  set(it.submittedLeaveRequestId, leaveRequestId)
                  set(it.assignedUserId, assignedUserId)
                  set(it.allowedStartDate, startDate)
                  set(it.allowedEndDate, endDate)
                  set(it.allowedDuration, totalLeave)
                  set(it.status, DataConstants.LeaveApprovalStatus.PENDING)
                }
              }

              // get leave type data
              val leaveType = database.from(LeaveTypeEntity).select().where {
                (LeaveTypeEntity.id eq leaveTypeId!!) and (LeaveTypeEntity.deletedFlag neq true)
              }.map { LeaveType.transform(it) }.firstOrNull()

              // add notification
              val notificationMessage = """
                Your <b>${leaveType?.name} Request</b> has been <b>sent</b> successfully!
              """.trimIndent()
              val notificationData = Notification(
                firstRelationId = leaveRequestId?.toString(),
                notificationType = DataConstants.NotificationType.LEAVE_REQUEST,
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
                (NotificationTokenEntity.userId eq userId) and (NotificationTokenEntity.deletedFlag neq true)
              }.map { NotificationToken.transform(it).notificationToken }.filterNotNull()

              PushNotification.sendMessage(
                title = DataConstants.NotificationTitle.LEAVE_APPLICATION_SENT,
                message = notificationData.message.toString(),
                tokenList = notificationTokens,
                data = notificationData
              )

              val message = "Successfully added leave request data"
              call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
            }
          } ?: run { call.returnFailedDatabaseResponse() }

        } catch (e: Exception) {
          e.printStackTrace()
          call.returnParameterErrorResponse(e.message)
          return@post
        }
      }

      post("my-leave-requests") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        val pagingRequest = call.receivePagingRequest<SubmittedLeavePagingRequest>()
        DBConnection.database?.let { database ->
          val submittedLeaveRequests = database.from(SubmittedLeaveRequestEntity)
            .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              (SubmittedLeaveRequestEntity.status inList pagingRequest.status) and (SubmittedLeaveRequestEntity.userId eq userId) and (SubmittedLeaveRequestEntity.deletedFlag neq true)
            }.orderBy(SubmittedLeaveRequestEntity.id.desc()).map {
              val submittedLeaveRequestId = it[SubmittedLeaveRequestEntity.id]!!
              val leaveApprovals = database.from(LeaveApprovalEntity)
                .leftJoin(UserEntity, on = UserEntity.id eq LeaveApprovalEntity.assignedUserId)
                .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
                .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
                .leftJoin(HumanCapitalManagementEntity, on = HumanCapitalManagementEntity.userId eq UserEntity.id)
                .select().where {
                  (LeaveApprovalEntity.submittedLeaveRequestId eq submittedLeaveRequestId) and (LeaveApprovalEntity.deletedFlag neq true)
                }.map { leaveApprovalRowSet ->
                  LeaveApproval.transform(leaveApprovalRowSet)
                }.filterNotNull()

              SubmittedLeaveRequest.transform(it, leaveApprovals = leaveApprovals, hideLeaveApprovals = true)
            }
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = submittedLeaveRequests,
              totalData = submittedLeaveRequests.size
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      get("leave-requests/detail/{submittedLeaveRequestId}") {
        val submittedLeaveRequestId = try {
          call.parameters["submittedLeaveRequestId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        DBConnection.database?.let { database ->
          database.from(SubmittedLeaveRequestEntity)
            .leftJoin(UserEntity, on = UserEntity.id eq SubmittedLeaveRequestEntity.userId)
            .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
            .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq SubmittedLeaveRequestEntity.leaveTypeId)
            .select().where {
              (SubmittedLeaveRequestEntity.id eq submittedLeaveRequestId) and (SubmittedLeaveRequestEntity.deletedFlag neq true)
            }
            .map {
              val onGoingProjects = database.from(OnGoingProjectEntity)
                .select().where {
                  (OnGoingProjectEntity.leaveRequestId eq submittedLeaveRequestId) and (OnGoingProjectEntity.deletedFlag neq true)
                }.orderBy(OnGoingProjectEntity.id.desc()).map { onGoingProjectRowSet ->
                  OnGoingProject.transform(onGoingProjectRowSet)
                }

              val leaveApprovals = database.from(LeaveApprovalEntity)
                .leftJoin(UserEntity, on = UserEntity.id eq LeaveApprovalEntity.assignedUserId)
                .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
                .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
                .leftJoin(HumanCapitalManagementEntity, on = HumanCapitalManagementEntity.userId eq UserEntity.id)
                .select().where {
                  (LeaveApprovalEntity.submittedLeaveRequestId eq submittedLeaveRequestId) and (LeaveApprovalEntity.deletedFlag neq true)
                }.map { leaveApprovalRowSet ->
                  LeaveApproval.transform(leaveApprovalRowSet)
                }.filterNotNull()

              SubmittedLeaveRequest.transform(it, onGoingProjects = onGoingProjects, leaveApprovals = leaveApprovals)
            }
            .firstOrNull()?.let { data ->
              val message = "Single data fetched successfully"
              call.respond(BaseResponse(success = true, message = message, data = data))
            } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}