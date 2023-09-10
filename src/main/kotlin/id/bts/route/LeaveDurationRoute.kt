package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.DivisionEntity
import id.bts.model.request.division.DivisionRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.division.Division
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.Instant

fun Application.configureLeaveDurationRoute() {
  routing {
    get("/divisions") {
      DBConnection.database?.let { database ->
        val divisions = database.from(DivisionEntity).select().where {
          DivisionEntity.deletedFlag neq true
        }.orderBy(DivisionEntity.id.desc()).map { Division.transform(it) }
        val message = "List data fetched successfully"
        call.respond(BaseResponse(success = true, message = message, data = divisions))
      } ?: run { returnFailedDatabaseResponse() }
    }

    get("/division/{divisionId}") {
      val divisionId = try {
        call.parameters["divisionId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@get
      }
      DBConnection.database?.let { database ->
        database.from(DivisionEntity).select().where {
          (DivisionEntity.id eq divisionId) and (DivisionEntity.deletedFlag neq true)
        }.map { Division.transform(it) }.firstOrNull()?.let { note ->
          val message = "Single data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = note))
        } ?: run { returnNotFoundResponse() }
      } ?: run { returnFailedDatabaseResponse() }
    }

    post("/division") {
      val divisionRequest = try {
        call.receive<DivisionRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@post
      }
      DBConnection.database?.let { database ->
        val affected = database.insert(DivisionEntity) {
          set(it.name, divisionRequest.name)
        }
        if (affected == 0) {
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menambahkan data divisi"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }

    put("/division/{divisionId}") {
      val divisionId = try {
        call.parameters["divisionId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@put
      }
      val divisionRequest = try {
        call.receive<DivisionRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
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
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil mengubah data divisi"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }

    delete("/division/{divisionId}") {
      val divisionId = try {
        call.parameters["divisionId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
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
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus data divisi"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
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
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus semua data divisi"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }
  }
}