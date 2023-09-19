package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.HumanCapitalManagementEntity
import id.bts.entities.UserEntity
import id.bts.model.request.PagingRequest
import id.bts.model.request.user.HCMRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.HCM
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

fun Application.configureHCMRoute() {
  routing {
    authenticate("user-authorization") {
      get("/hcm/{hcmId}") {
        val hcmId = try {
          call.parameters["hcmId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }
        DBConnection.database?.let { database ->
          database.from(HumanCapitalManagementEntity)
            .leftJoin(UserEntity, on = UserEntity.id eq HumanCapitalManagementEntity.userId)
            .select().where {
              (HumanCapitalManagementEntity.id eq hcmId) and (HumanCapitalManagementEntity.deletedFlag neq true)
            }.map { HCM.transform(it) }.firstOrNull()?.let { data ->
              val message = "Single data fetched successfully"
              call.respond(BaseResponse(success = true, message = message, data = data))
            } ?: run { call.returnNotFoundResponse() }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      post("/hcm-list") {
        val pagingRequest = call.receivePagingRequest<PagingRequest>()
        DBConnection.database?.let { database ->
          val hcmList = database.from(HumanCapitalManagementEntity)
            .leftJoin(UserEntity, on = UserEntity.id eq HumanCapitalManagementEntity.userId)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              HumanCapitalManagementEntity.deletedFlag neq true
            }.orderBy(HumanCapitalManagementEntity.id.desc()).map { HCM.transform(it) }.filterNotNull()
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = hcmList,
              totalData = hcmList.size
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    authenticate("admin-authorization") {
      post("/hcm") {
        val hcmRequest = try {
          call.receive<HCMRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }
        DBConnection.database?.let { database ->
          val existingHCM = database.from(HumanCapitalManagementEntity).select().where {
            (HumanCapitalManagementEntity.userId eq hcmRequest.userId) and
              (HumanCapitalManagementEntity.deletedFlag neq true)
          }.totalRecordsInAllPages

          if (existingHCM > 0) {
            call.returnParameterErrorResponse("HCM data already exist")
            return@post
          }

          val affected = database.insert(HumanCapitalManagementEntity) {
            set(it.userId, hcmRequest.userId)
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "HCM data has been added successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/hcm/{hcmId}") {
        val hcmId = try {
          call.parameters["hcmId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        val hcmRequest = try {
          call.receive<HCMRequest>()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }
        DBConnection.database?.let { database ->
          val affected = database.update(HumanCapitalManagementEntity) {
            set(it.userId, hcmRequest.userId)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq hcmId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "HCM data has been updated successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/hcm/{hcmId}") {
        val hcmId = try {
          call.parameters["hcmId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }
        DBConnection.database?.let { database ->
          val affected = database.update(HumanCapitalManagementEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq hcmId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "HCM data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/hcm-list") {
        DBConnection.database?.let { database ->
          val affected = database.update(HumanCapitalManagementEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              it.deletedFlag neq true
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All HCM data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}