package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.SuperVisorEntity
import id.bts.entities.UserEntity
import id.bts.model.request.PagingRequest
import id.bts.model.request.user.SuperVisorRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.SuperVisor
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

fun Application.configureSuperVisorRoute() {
  routing {
    authenticate("user-authorization") {
      get("/super-visor/{superVisorId}") {
        val superVisorId = try {
          call.parameters["superVisorId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }
        DBConnection.database?.let { database ->
          database.from(SuperVisorEntity)
            .leftJoin(UserEntity, on = UserEntity.id eq SuperVisorEntity.userId)
            .select().where {
              (SuperVisorEntity.id eq superVisorId) and (SuperVisorEntity.deletedFlag neq true)
            }.map { SuperVisor.transform(it) }.firstOrNull()?.let { data ->
              val message = "Single data fetched successfully"
              call.respond(BaseResponse(success = true, message = message, data = data))
            } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      post("/super-visors") {
        val pagingRequest = call.receivePagingRequest<PagingRequest>()
        DBConnection.database?.let { database ->
          val superVisors = database.from(SuperVisorEntity)
            .leftJoin(UserEntity, on = UserEntity.id eq SuperVisorEntity.userId)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              SuperVisorEntity.deletedFlag neq true
            }.orderBy(SuperVisorEntity.id.desc()).map { SuperVisor.transform(it) }
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = superVisors,
              totalData = superVisors.size
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    authenticate("admin-authorization") {
      post("/super-visor") {
        val superVisorRequest = try {
          call.receive<SuperVisorRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }
        DBConnection.database?.let { database ->
          val existingSuperVisor = database.from(SuperVisorEntity).select().where {
            (SuperVisorEntity.userId eq superVisorRequest.userId) and
              (SuperVisorEntity.deletedFlag neq true)
          }.totalRecordsInAllPages

          if (existingSuperVisor > 0) {
            call.returnParameterErrorResponse("Super visor data already exist")
            return@post
          }

          val affected = database.insert(SuperVisorEntity) {
            set(it.userId, superVisorRequest.userId)
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Super visor data has been added successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/super-visor/{superVisorId}") {
        val superVisorId = try {
          call.parameters["superVisorId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        val superVisorRequest = try {
          call.receive<SuperVisorRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        DBConnection.database?.let { database ->
          val affected = database.update(SuperVisorEntity) {
            set(it.userId, superVisorRequest.userId)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq superVisorId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Super visor data has been updated successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/super-visor/{superVisorId}") {
        val superVisorId = try {
          call.parameters["superVisorId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(SuperVisorEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq superVisorId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Super visor data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/super-visors") {
        DBConnection.database?.let { database ->
          val affected = database.update(SuperVisorEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              it.deletedFlag neq true
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All super visor data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}