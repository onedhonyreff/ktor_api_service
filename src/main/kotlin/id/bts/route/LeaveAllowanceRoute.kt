package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.LeaveAllowanceEntity
import id.bts.entities.LeaveTypeEntity
import id.bts.entities.UserEntity
import id.bts.model.request.PagingRequest
import id.bts.model.request.leave_allowance.LeaveAllowanceRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_allowance.LeaveAllowance
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.ktorm.database.asIterable
import org.ktorm.dsl.*
import java.time.Instant

fun Application.configureLeaveAllowanceRoute() {
  routing {
    authenticate("user-authorization") {
      post("/my-leave-allowances") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        call.getUserLeaveAllowanceList(userId)
      }

      get("/my-leave-allowance") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        val leaveTypeId = try {
          call.request.queryParameters["leave_type_id"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        call.getUserLeaveAllowance(userId, leaveTypeId)
      }

      post("/leave-allowances") {
        val userId = try {
          call.request.queryParameters["user_id"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        call.getUserLeaveAllowanceList(userId, true)
      }

      get("/leave-allowance") {
        val userId = try {
          call.request.queryParameters["user_id"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        val leaveTypeId = try {
          call.request.queryParameters["leave_type_id"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        call.getUserLeaveAllowance(userId, leaveTypeId, true)
      }
    }

    authenticate("admin-authorization") {
      post("/leave-allowance") {
        val leaveAllowanceRequest = try {
          call.receive<LeaveAllowanceRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        DBConnection.database?.let { database ->
          val existingLeaveAllowance = database.from(LeaveAllowanceEntity).select().where {
            (LeaveAllowanceEntity.userId eq leaveAllowanceRequest.userId) and
              (LeaveAllowanceEntity.leaveTypeId eq leaveAllowanceRequest.leaveTypeId) and
              (LeaveAllowanceEntity.deletedFlag neq true)
          }.totalRecordsInAllPages

          if (existingLeaveAllowance > 0) {
            call.returnParameterErrorResponse("Leave allowance data already exist")
            return@post
          }

          val leaveType = database.from(LeaveTypeEntity).select().where {
            (LeaveTypeEntity.id eq leaveAllowanceRequest.leaveTypeId) and (LeaveTypeEntity.deletedFlag neq true)
          }.orderBy(LeaveTypeEntity.id.desc()).map { LeaveType.transform(it) }.firstOrNull()

          val duration = leaveAllowanceRequest.duration ?: leaveType?.defaultDuration

          val affected = try {
            database.insert(LeaveAllowanceEntity) {
              set(it.userId, leaveAllowanceRequest.userId)
              set(it.leaveTypeId, leaveAllowanceRequest.leaveTypeId)
              set(it.duration, duration)
            }
          } catch (e: Exception) {
            call.returnNotImplementedResponse(e.message)
            return@post
          }

          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "User leave allowance data has been added successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/leave-allowance/{leaveAllowanceId}") {
        val leaveAllowanceId = try {
          call.parameters["leaveAllowanceId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        val leaveAllowanceRequest = try {
          call.receive<LeaveAllowanceRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val affected = database.update(LeaveAllowanceEntity) {
            set(it.userId, leaveAllowanceRequest.userId)
            set(it.leaveTypeId, leaveAllowanceRequest.leaveTypeId)
            set(it.duration, leaveAllowanceRequest.duration)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq leaveAllowanceId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "User leave allowance data has been updated successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/leave-allowance/{leaveAllowanceId}") {
        val leaveAllowanceId = try {
          call.parameters["leaveAllowanceId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(LeaveAllowanceEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq leaveAllowanceId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "User leave allowance data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}

fun ApplicationCall.getUserLeaveAllowanceList(userId: Int, includeUser: Boolean = false) {
  runBlocking {
    val pagingRequest = receivePagingRequest<PagingRequest>()
    DBConnection.database?.let { database ->
      val usedDurations = hashMapOf<String, Int>()
      val queryString = generateUsedDurationQueryString(userId)
      try {
        database.useConnection { conn ->
          conn.prepareStatement(queryString).use { statement ->
            statement.executeQuery().asIterable().map { data ->
              usedDurations[data.getString(1)] = data.getInt(3)
            }
          }
        }
      } catch (e: Exception) {
        respond(
          HttpStatusCode.InternalServerError,
          BaseResponse(success = false, message = "Error", null)
        )
        return@runBlocking
      }

      val userLeaveAllowances = database.from(LeaveAllowanceEntity)
        .leftJoin(UserEntity, on = UserEntity.id eq LeaveAllowanceEntity.userId)
        .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq LeaveAllowanceEntity.leaveTypeId)
        .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
          (LeaveAllowanceEntity.userId eq userId) and (LeaveAllowanceEntity.deletedFlag neq true)
        }.orderBy(LeaveAllowanceEntity.id.desc()).map {
          val leaveTypeId = it[LeaveAllowanceEntity.leaveTypeId].toString()
          LeaveAllowance.transform(it, includeUser = includeUser, usedDuration = usedDurations[leaveTypeId])
        }
      val message = "User leave allowance list data fetched successfully"
      respond(
        BaseResponse(
          success = true,
          message = message,
          data = userLeaveAllowances,
          totalData = userLeaveAllowances.size
        )
      )
    } ?: run { returnFailedDatabaseResponse() }
  }
}

fun ApplicationCall.getUserLeaveAllowance(
  userId: Int,
  leaveTypeId: Int,
  includeUser: Boolean = false
) {
  runBlocking {
    DBConnection.database?.let { database ->
      val usedDurations = hashMapOf<String, Int>()
      val queryString = generateUsedDurationQueryString(userId)
      try {
        database.useConnection { conn ->
          conn.prepareStatement(queryString).use { statement ->
            statement.executeQuery().asIterable().map { data ->
              usedDurations[data.getString(1)] = data.getInt(3)
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        respond(
          HttpStatusCode.InternalServerError,
          BaseResponse(success = false, message = "Error", null)
        )
        return@runBlocking
      }

      database.from(LeaveAllowanceEntity)
        .leftJoin(UserEntity, on = UserEntity.id eq LeaveAllowanceEntity.userId)
        .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq LeaveAllowanceEntity.leaveTypeId)
        .select().where {
          (LeaveAllowanceEntity.userId eq userId) and
            (LeaveAllowanceEntity.leaveTypeId eq leaveTypeId) and
            (LeaveAllowanceEntity.deletedFlag neq true)
        }
        .orderBy(LeaveAllowanceEntity.id.desc())
        .map {
          LeaveAllowance.transform(it, includeUser = includeUser, usedDuration = usedDurations[leaveTypeId.toString()])
        }
        .firstOrNull()?.let { userLeaveAllowance ->
          val message = "User leave allowance data fetched successfully"
          respond(BaseResponse(success = true, message = message, data = userLeaveAllowance))
        } ?: run { returnNotFoundResponse() }
    } ?: run { returnFailedDatabaseResponse() }
  }
}

fun generateUsedDurationQueryString(userId: Int): String {
  return """
        select 
        leave_types.id, leave_types.name, sum(approved_leaves.used_durations) as used_durations
        from leave_durations
        left join leave_types on leave_types.id = leave_durations.leave_type_id
        left join (
	        select leave_requests.id, leave_requests.leave_type_id , min(leave_approvals.allowed_duration) as used_durations
	        from leave_requests and leave_requests.status != 'CANCELED'
	        left join leave_approvals on leave_approvals.leave_request_id = leave_requests.id
	        where leave_requests.user_id = $userId
	        group by leave_requests.id
	        having sum(case when (leave_approvals.status = 'REJECTED') or ((leave_approvals.status = 'PENDING' or leave_approvals.status = 'CANCELED') and leave_requests.start_date <= now()) then 1 else 0 end) = 0
        ) as approved_leaves on approved_leaves.leave_type_id = leave_types.id
        where leave_durations.user_id = $userId
        group by leave_types.id
      """
}