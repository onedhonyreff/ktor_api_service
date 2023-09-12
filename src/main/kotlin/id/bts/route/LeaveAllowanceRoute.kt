package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.LeaveAllowanceEntity
import id.bts.entities.LeaveTypeEntity
import id.bts.model.request.leave_allowance.LeaveAllowanceRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_allowance.LeaveAllowance
import id.bts.model.response.simple_message.SimpleMessage
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
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import org.ktorm.dsl.*
import java.time.Instant

fun Application.configureLeaveAllowanceRoute() {
  routing {
    authenticate("user-authorization") {
      get("/my-leave-allowances") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        getUserLeaveAllowanceList(userId)
      }

      get("/my-leave-allowances") {
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

        getUserLeaveAllowance(userId, leaveTypeId)
      }

      get("/leave-allowances") {
        val userId = try {
          call.request.queryParameters["user_id"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        getUserLeaveAllowanceList(userId)
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

        getUserLeaveAllowance(userId, leaveTypeId)
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
          val affected = database.insert(LeaveAllowanceEntity) {
            set(it.userId, leaveAllowanceRequest.userId)
            set(it.leaveTypeId, leaveAllowanceRequest.leaveTypeId)
            set(it.duration, leaveAllowanceRequest.duration)
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

fun PipelineContext<Unit, ApplicationCall>.getUserLeaveAllowanceList(userId: Int) {
  runBlocking {
    DBConnection.database?.let { database ->
      val userLeaveAllowances = database.from(LeaveAllowanceEntity)
        .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq LeaveAllowanceEntity.leaveTypeId)
        .select().where {
          (LeaveAllowanceEntity.userId eq userId) and (LeaveAllowanceEntity.deletedFlag neq true)
        }.orderBy(LeaveAllowanceEntity.id.desc()).map { LeaveAllowance.transform(it, includeUser = false) }
      val message = "User leave allowance list data fetched successfully"
      call.respond(BaseResponse(success = true, message = message, data = userLeaveAllowances))
    } ?: run { call.returnFailedDatabaseResponse() }
  }
}

fun PipelineContext<Unit, ApplicationCall>.getUserLeaveAllowance(userId: Int, leaveTypeId: Int) {
  runBlocking {
    DBConnection.database?.let { database ->
      database.from(LeaveAllowanceEntity)
        .leftJoin(LeaveTypeEntity, on = LeaveTypeEntity.id eq LeaveAllowanceEntity.leaveTypeId)
        .select().where {
          (LeaveAllowanceEntity.userId eq userId) and
            (LeaveTypeEntity.id eq leaveTypeId) and
            (LeaveAllowanceEntity.deletedFlag neq true)
        }
        .orderBy(LeaveAllowanceEntity.id.desc())
        .map { LeaveAllowance.transform(it, includeUser = false) }
        .firstOrNull()?.let { userLeaveAllowance ->
          val message = "User leave allowance data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = userLeaveAllowance))
        } ?: run { call.returnNotFoundResponse() }
    } ?: run { call.returnFailedDatabaseResponse() }
  }
}