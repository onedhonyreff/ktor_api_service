package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.NotificationEntity
import id.bts.model.request.notification.NotificationPagingRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.notification.Notification
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.utils.Extensions.receivePagingRequest
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

fun Application.configureNotificationRoute() {
  routing {
    authenticate("user-authorization") {
      post("/my-notifications") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@post
        }

        val pagingRequest = call.receivePagingRequest<NotificationPagingRequest>()
        DBConnection.database?.let { database ->
          val zoneId = ZoneId.of("Asia/Jakarta")
          val yesterday = Instant.now().atZone(zoneId).minus(Duration.ofDays(1)).toInstant()
          val oneYearAgo = Instant.now().atZone(zoneId).minus(Duration.ofDays(365)).toInstant()
          val notifications = database.from(NotificationEntity)
            .select().limit(pagingRequest.size).offset(pagingRequest.pagingOffset).where {
              (NotificationEntity.userId eq userId) and (NotificationEntity.deletedFlag neq true) and
                (pagingRequest.readed?.let { NotificationEntity.readed eq it } ?: NotificationEntity.id.isNotNull()) and
                (pagingRequest.isNew?.let { if (it) NotificationEntity.createdAt greater yesterday else ((NotificationEntity.createdAt lessEq yesterday) and (NotificationEntity.createdAt greater oneYearAgo)) }
                  ?: NotificationEntity.id.isNotNull())
            }.orderBy(NotificationEntity.id.desc()).map {
              Notification.transform(it)
            }
          val message = "List data fetched successfully"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = notifications,
              totalData = notifications.size
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/notification/read/{notificationId}") {
        val userId: Int
        val notificationId: Int
        try {
          userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
          notificationId = call.parameters["notificationId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val affected = database.update(NotificationEntity) {
            set(it.readed, true)
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq notificationId) and (it.userId eq userId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Notification data has been readed successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/notifications/read") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val affected = database.update(NotificationEntity) {
            set(it.readed, true)
            set(it.updatedAt, Instant.now())
            where {
              (it.userId eq userId) and (it.readed eq false) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All notification data has been readed successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/notification/{notificationId}") {
        val userId: Int
        val notificationId: Int
        try {
          userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
          notificationId = call.parameters["notificationId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }

        DBConnection.database?.let { database ->
          val affected = database.update(NotificationEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.id eq notificationId) and (it.userId eq userId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "Notification data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      delete("/notifications") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@delete
        }

        DBConnection.database?.let { database ->
          val affected = database.update(NotificationEntity) {
            set(it.deletedAt, Instant.now())
            set(it.deletedFlag, true)
            where {
              (it.userId eq userId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "All notification data has been deleted successfully"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}