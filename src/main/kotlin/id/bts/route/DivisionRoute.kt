package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.DivisionEntity
import id.bts.model.request.division.DivisionRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.division.Division
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

fun Application.configureDivisionRoute() {
  routing {
    authenticate("user-authorization") {
      get("/division/{divisionId}") {
        val divisionId = try {
          call.parameters["divisionId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }
        DBConnection.database?.let { database ->
          database.from(DivisionEntity).select().where {
            (DivisionEntity.id eq divisionId) and (DivisionEntity.deletedFlag neq true)
          }.map { Division.transform(it) }.firstOrNull()?.let { note ->
            val message = "Single data fetched successfully"
            call.respond(BaseResponse(success = true, message = message, data = note))
          } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      post("/divisions") {
        val pagingRequest = call.receivePagingRequest()
        DBConnection.database?.let { database ->
          val divisions = database.from(DivisionEntity)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              DivisionEntity.deletedFlag neq true
            }.orderBy(DivisionEntity.id.desc()).map { Division.transform(it) }
          val message = "List data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = divisions, totalData = divisions.size))
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    authenticate("admin-authorization") {
      post("/division") {
        val divisionRequest = try {
          call.receive<DivisionRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }
        DBConnection.database?.let { database ->
          val affected = database.insert(DivisionEntity) {
            set(it.name, divisionRequest.name)
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Division data has been added successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/division/{divisionId}") {
        val divisionId = try {
          call.parameters["divisionId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        val divisionRequest = try {
          call.receive<DivisionRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        DBConnection.database?.let { database ->
          val affected = database.update(DivisionEntity) {
            set(it.name, divisionRequest.name)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq divisionId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Division data has been updated successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/division/{divisionId}") {
        val divisionId = try {
          call.parameters["divisionId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(DivisionEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq divisionId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Division data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/divisions") {
        DBConnection.database?.let { database ->
          val affected = database.update(DivisionEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              it.deletedFlag neq true
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All division data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}