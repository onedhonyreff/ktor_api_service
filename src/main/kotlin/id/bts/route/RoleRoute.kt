package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.RoleEntity
import id.bts.model.request.role.RoleRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.role.Role
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.Instant

fun Application.configureRoleRoute() {
  routing {
    authenticate("user-authorization") {
      get("/role/{roleId}") {
        val roleId = try {
          call.parameters["roleId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }
        DBConnection.database?.let { database ->
          database.from(RoleEntity).select().where {
            (RoleEntity.id eq roleId) and (RoleEntity.deletedFlag neq true)
          }.map { Role.transform(it) }.firstOrNull()?.let { data ->
            val message = "Single data fetched successfully"
            call.respond(BaseResponse(success = true, message = message, data = data))
          } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      post("/roles") {
        val pagingRequest = call.receivePagingRequest()
        DBConnection.database?.let { database ->
          val roles = database.from(RoleEntity)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              RoleEntity.deletedFlag neq true
            }.orderBy(RoleEntity.id.desc()).map { Role.transform(it) }.filterNotNull()
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(success = true, message = message, data = roles, totalData = roles.size)
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    authenticate("admin-authorization") {
      post("/role") {
        val roleRequest = try {
          call.receive<RoleRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }
        DBConnection.database?.let { database ->
          val affected = database.insert(RoleEntity) {
            set(it.name, roleRequest.name)
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Role data has been added successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/role/{roleId}") {
        val roleId = try {
          call.parameters["roleId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        val roleRequest = try {
          call.receive<RoleRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        DBConnection.database?.let { database ->
          val affected = database.update(RoleEntity) {
            set(it.name, roleRequest.name)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq roleId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Role data has been updated successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/role/{roleId}") {
        val roleId = try {
          call.parameters["roleId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(RoleEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq roleId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Role data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/roles") {
        DBConnection.database?.let { database ->
          val affected = database.update(RoleEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              it.deletedFlag neq true
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All Role data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}