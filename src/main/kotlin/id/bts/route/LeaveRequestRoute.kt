package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.*
import id.bts.model.request.leave_request.SubmittedLeavePagingRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_request.SubmittedLeaveRequest
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.DataConstants
import id.bts.utils.DateTimeUtils.parseToDate
import id.bts.utils.DateTimeUtils.parseToLocalDate
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.replaceFileName
import id.bts.utils.Extensions.returnFailedDatabaseResponse
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

            onGoingProjects?.forEach { onGoingProject ->
              database.insert(OnGoingProjectEntity) {
                set(it.leaveRequestId, leaveRequestId)
                set(it.name, onGoingProject)
              }
            }

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

            if (leaveRequestId == 0) {
              call.returnNotImplementedResponse()
            } else {
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
            }.orderBy(SubmittedLeaveRequestEntity.id.desc()).map { SubmittedLeaveRequest.transform(it) }
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
      }
    }
  }
}