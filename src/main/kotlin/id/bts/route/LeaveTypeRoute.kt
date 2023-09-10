package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.LeaveTypeEntity
import id.bts.model.response.BaseResponse
import id.bts.model.response.leave_type.LeaveType
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.Extensions.replaceFileName
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import id.bts.utils.ProjectUtil
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.io.File
import java.time.Instant

fun Application.configureLeaveTypeRoute() {
  routing {
    get("/leave-types") {
      DBConnection.database?.let { database ->
        val leaveTypes = database.from(LeaveTypeEntity).select().where {
          LeaveTypeEntity.deletedFlag neq true
        }.orderBy(LeaveTypeEntity.id.desc()).map { LeaveType.transform(it) }
        val message = "List data fetched successfully"
        call.respond(BaseResponse(success = true, message = message, data = leaveTypes))
      } ?: run { returnFailedDatabaseResponse() }
    }

    get("/leave-type/{leaveTypeId}") {
      val leaveTypeId = try {
        call.parameters["leaveTypeId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@get
      }
      DBConnection.database?.let { database ->
        database.from(LeaveTypeEntity).select().where {
          (LeaveTypeEntity.id eq leaveTypeId) and (LeaveTypeEntity.deletedFlag neq true)
        }.map { LeaveType.transform(it) }.firstOrNull()?.let { note ->
          val message = "Single data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = note))
        } ?: run { returnNotFoundResponse() }
      } ?: run { returnFailedDatabaseResponse() }
    }

    post("/leave-type") {
      val multipart = try {
        call.receiveMultipart()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@post
      }

      var initialName: String? = null
      var name: String? = null
      var description: String? = null
      var defaultDuration: Int? = null
      var illustrationUrl: String? = null

      try {
        multipart.forEachPart { part ->
          when (part) {
            is PartData.FileItem -> {
              when (part.name) {
                "illustration" -> {
                  val inputStream = part.streamProvider()
                  val filename = part.originalFileName.replaceFileName()

                  val directory = File("${ProjectUtil.getProjectDir()}/assets/images/leave_type")
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
            set(it.illustration, illustrationUrl)
          }
          if (affected == 0) {
            returnNotImplementedResponse()
          } else {
            val message = "Berhasil menambahkan data tipe cuti"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { returnFailedDatabaseResponse() }

      } catch (e: Exception) {
        e.printStackTrace()
        returnParameterErrorResponse(e.message)
        return@post
      }
    }

    put("/leave-type/{leaveTypeId}") {
      val leaveTypeId = try {
        call.parameters["leaveTypeId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@put
      }
      val multipart = try {
        call.receiveMultipart()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@put
      }

      var initialName: String? = null
      var name: String? = null
      var description: String? = null
      var defaultDuration: Int? = null
      var illustrationUrl: String? = null

      try {
        multipart.forEachPart { part ->
          when (part) {
            is PartData.FileItem -> {
              when (part.name) {
                "illustration" -> {
                  val inputStream = part.streamProvider()
                  val filename = part.originalFileName.replaceFileName()

                  val directory = File("${ProjectUtil.getProjectDir()}/assets/images/leave_type")
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
              }
            }

            else -> Unit
          }
        }

        DBConnection.database?.let { database ->
          val affected = database.update(LeaveTypeEntity) {
            initialName?.let { data -> set(it.initial_name, data) }
            name?.let { data -> set(it.name, data) }
            description?.let { data -> set(it.description, data) }
            defaultDuration?.let { data -> set(it.defaultDuration, data) }
            illustrationUrl?.let { data -> set(it.illustration, data) }
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq leaveTypeId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            returnNotImplementedResponse()
          } else {
            val message = "Berhasil mengubah data tipe cuti"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { returnFailedDatabaseResponse() }

      } catch (e: Exception) {
        e.printStackTrace()
        returnParameterErrorResponse(e.message)
        return@put
      }
    }

    delete("/leave-type/{leaveTypeId}") {
      val leaveTypeId = try {
        call.parameters["leaveTypeId"]!!.toInt()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
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
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus data tipe cuti"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
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
          returnNotImplementedResponse()
        } else {
          val message = "Berhasil menghapus semua data tipe cuti"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { returnFailedDatabaseResponse() }
    }
  }
}