package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.LeaveTypeEntity
import id.bts.model.request.PagingRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.simple_message.SimpleMessage
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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.io.File
import java.time.Instant

fun Application.configureLeaveTypeRoute() {
  routing {
    authenticate("user-authorization") {
      get("/leave-type/{leaveTypeId}") {
        val leaveTypeId = try {
          call.parameters["leaveTypeId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }
        DBConnection.database?.let { database ->
          database.from(LeaveTypeEntity).select().where {
            (LeaveTypeEntity.id eq leaveTypeId) and (LeaveTypeEntity.deletedFlag neq true)
          }.map { LeaveType.transform(it) }.firstOrNull()?.let { leaveType ->
            val message = "Single data fetched successfully"
            call.respond(BaseResponse(success = true, message = message, data = leaveType))
          } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      post("/leave-types") {
        val pagingRequest = call.receivePagingRequest<PagingRequest>()
        DBConnection.database?.let { database ->
          val leaveTypes = database.from(LeaveTypeEntity)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              LeaveTypeEntity.deletedFlag neq true
            }.orderBy(LeaveTypeEntity.id.desc()).map { LeaveType.transform(it) }.filterNotNull()
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(success = true, message = message, data = leaveTypes, totalData = leaveTypes.size)
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    authenticate("admin-authorization") {
      post("/leave-type") {
        val multipart = try {
          call.receiveMultipart()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        var initialName: String? = null
        var name: String? = null
        var description: String? = null
        var defaultDuration: Int? = null
        var note: String? = null
        var illustrationUrl: String? = null

        try {
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FileItem -> {
                when (part.name) {
                  "illustration" -> {
                    val inputStream = part.streamProvider()
                    val filename = part.originalFileName.replaceFileName()

                    val directory = File("${ProjectUtils.getProjectDir()}/assets/images/leave_type")
                    if (!directory.exists()) {
                      directory.mkdir()
                    }

                    val illustrationPath = "$directory/$filename"
                    val destinationFile = File(illustrationPath)
                    inputStream.copyTo(destinationFile.outputStream().buffered())
                    illustrationUrl = "/file/open/image/leave_type/$filename"
                  }
                }
              }

              is PartData.FormItem -> {
                when (part.name) {
                  "initial_name" -> initialName = part.value
                  "name" -> name = part.value
                  "description" -> description = part.value
                  "default_duration" -> defaultDuration = part.value.toInt()
                  "note" -> note = part.value
                }
              }

              else -> Unit
            }
          }

          DBConnection.database?.let { database ->
            val affected = database.insert(LeaveTypeEntity) {
              set(it.initial_name, initialName)
              set(it.name, name)
              set(it.description, description)
              set(it.defaultDuration, defaultDuration)
              set(it.note, note)
              set(it.illustration, illustrationUrl)
            }
            if (affected == 0) {
              call.returnNotImplementedResponse()
            } else {
              val message = "Leave type data has been added successfully"
              call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
            }
          } ?: run { call.returnFailedDatabaseResponse() }

        } catch (e: Exception) {
          e.printStackTrace()
          call.returnParameterErrorResponse(e.message)
          return@post
        }
      }

      put("/leave-type/{leaveTypeId}") {
        val leaveTypeId = try {
          call.parameters["leaveTypeId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        val multipart = try {
          call.receiveMultipart()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        var initialName: String? = null
        var name: String? = null
        var description: String? = null
        var defaultDuration: Int? = null
        var note: String? = null
        var illustrationUrl: String? = null

        try {
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FileItem -> {
                when (part.name) {
                  "illustration" -> {
                    val inputStream = part.streamProvider()
                    val filename = part.originalFileName.replaceFileName()

                    val directory = File("${ProjectUtils.getProjectDir()}/assets/images/leave_type")
                    if (!directory.exists()) {
                      directory.mkdir()
                    }

                    val illustrationPath = "$directory/$filename"
                    val destinationFile = File(illustrationPath)
                    inputStream.copyTo(destinationFile.outputStream().buffered())
                    illustrationUrl = "/file/open/image/leave_type/$filename"
                  }
                }
              }

              is PartData.FormItem -> {
                when (part.name) {
                  "initial_name" -> initialName = part.value
                  "name" -> name = part.value
                  "description" -> description = part.value
                  "default_duration" -> defaultDuration = part.value.toInt()
                  "note" -> note = part.value
                }
              }

              else -> Unit
            }
          }

          DBConnection.database?.let { database ->
            val affected = database.update(LeaveTypeEntity) {
              initialName?.let { data -> set(it.initial_name, data) }
              name?.let { data -> set(it.name, data) }
              set(it.description, description)
              set(it.defaultDuration, defaultDuration)
              illustrationUrl?.let { data -> set(it.illustration, data) }
              set(it.note, note)
              set(it.updatedAt, Instant.now())
              where {
                (it.id eq leaveTypeId) and (it.deletedFlag neq true)
              }
            }
            if (affected == 0) {
              call.returnNotImplementedResponse()
            } else {
              val message = "Leave type data has been updated successfully"
              call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
            }
          } ?: run { call.returnFailedDatabaseResponse() }

        } catch (e: Exception) {
          e.printStackTrace()
          call.returnParameterErrorResponse(e.message)
          return@put
        }
      }

      delete("/leave-type/{leaveTypeId}") {
        val leaveTypeId = try {
          call.parameters["leaveTypeId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(LeaveTypeEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq leaveTypeId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Leave type data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/leave-types") {
        DBConnection.database?.let { database ->
          val affected = database.update(LeaveTypeEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              it.deletedFlag neq true
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All leave type data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}