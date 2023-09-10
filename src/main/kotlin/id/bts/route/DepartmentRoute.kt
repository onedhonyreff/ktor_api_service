package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.DepartmentEntity
import id.bts.model.request.department.DepartmentRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.department.Department
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

fun Application.configureDepartmentRoute() {
  routing {
    get("/departments") {
      DBConnection.database?.let { database ->
        val departments = database.from(DepartmentEntity).select().where {
          DepartmentEntity.deletedFlag neq true
        }.orderBy(DepartmentEntity.id.desc()).map { Department.transform(it) }
        val message = "List data fetched successfully"
        call.respond(BaseResponse(success = true, message = message, data = departments))
      } ?: run { returnFailedDatabaseResponse() }
    }

    get("/department/{departmentId}") {
      val departmentId = try {
        call.parameters["departmentId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@get
      }
      DBConnection.database?.let { database ->
        database.from(DepartmentEntity).select().where {
          (DepartmentEntity.id eq departmentId) and (DepartmentEntity.deletedFlag neq true)
        }.map { Department.transform(it) }.firstOrNull()?.let { note ->
          val message = "Single data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = note))
        } ?: run { returnNotFoundResponse() }
      } ?: run { returnFailedDatabaseResponse() }
    }

    post("/department") {
      val departmentRequest = try {
        call.receive<DepartmentRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@post
      }
      DBConnection.database?.let { database ->
        val affected = database.insert(DepartmentEntity) {
          set(it.name, departmentRequest.name)
        }
        if (affected == 0) {
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menambahkan data departemen"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }

    put("/department/{departmentId}") {
      val departmentId = try {
        call.parameters["departmentId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@put
      }
      val departmentRequest = try {
        call.receive<DepartmentRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@put
      }
      DBConnection.database?.let { database ->
        val affected = database.update(DepartmentEntity) {
          set(it.name, departmentRequest.name)
          set(it.updatedAt, Instant.now())
          where {
            (it.id eq departmentId) and (it.deletedFlag neq true)
          }
        }
        if (affected == 0) {
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil mengubah data departemen"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }

    delete("/department/{departmentId}") {
      val departmentId = try {
        call.parameters["departmentId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@delete
      }
      DBConnection.database?.let { database ->
        val affected = database.update(DepartmentEntity) {
          set(it.deletedAt, Instant.now())
          set(it.deletedFlag, true)
          where {
            (it.id eq departmentId) and (it.deletedFlag neq true)
          }
        }
        if (affected == 0) {
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus data departemen"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }

    delete("/departments") {
      DBConnection.database?.let { database ->
        val affected = database.update(DepartmentEntity) {
          set(it.deletedAt, Instant.now())
          set(it.deletedFlag, true)
          where {
            it.deletedFlag neq true
          }
        }
        if (affected == 0) {
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus data departemen"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }
  }
}