package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.UserEntity
import id.bts.manager.TokenManager
import id.bts.model.request.auth.ChangePasswordRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.User
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import id.bts.utils.Extensions.returnUnauthorizedResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

fun Application.configureUserPasswordRoute() {
  routing {
    post("/forgot-password") {
      val changePasswordRequest = try {
        val request = call.receive<ChangePasswordRequest>()
        if (request.email.isNullOrBlank()) throw Exception("Required Field - Email")
        request
      } catch (e: Exception) {
        call.returnParameterErrorResponse(e.message)
        return@post
      }

      DBConnection.database?.let { database ->
        val isUserExists = database.from(UserEntity).select().where {
          (UserEntity.email eq changePasswordRequest.email!!) and (UserEntity.deletedFlag neq true)
        }.totalRecordsInAllPages > 0

        if (!isUserExists) {
          call.returnNotFoundResponse("Email not registered. Try to check your email address.")
        } else {

          // TODO send email message include OTP number for requested email.

          val message = "We have sent an OTP code. Check the message in your email to reset your account password."
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { call.returnFailedDatabaseResponse() }
    }

    put("/reset-password") {
      val email: String
      val otp: String
      val changePasswordRequest = try {
        val request = call.receive<ChangePasswordRequest>()
        if (request.newPassword.isNullOrBlank()) throw Exception("Required Field - New Password")
        request
      } catch (e: Exception) {
        call.returnParameterErrorResponse(e.message)
        return@put
      }

      try {
        val decodedToken = TokenManager.decode(changePasswordRequest.accessToken!!, TokenManager.secretForgotPassword)
        email = decodedToken.getClaim("email").asString()
        otp = decodedToken.getClaim("otp").asString()
      } catch (e: Exception) {
        call.returnUnauthorizedResponse()
        return@put
      }

      DBConnection.database?.let { database ->
        // TODO change verification
        if (otp != "000000") {
          call.returnUnauthorizedResponse("Reset password failed. Try again from forgot password request")
          return@put
        }

        val affected = database.update(UserEntity) {
          set(it.password, User.hashPassword(changePasswordRequest.newPassword!!))
          set(it.updatedAt, Instant.now())
          where {
            (it.email eq email) and (it.deletedFlag neq true)
          }
        }
        if (affected == 0) {
          call.returnNotImplementedResponse()
        } else {
          val message = "You have successfully reset your password"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
        }
      } ?: run { call.returnFailedDatabaseResponse() }
    }

    authenticate("user-authorization") {
      put("/change-password") {
        val userId: Int
        val changePasswordRequest: ChangePasswordRequest
        try {
          userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
          changePasswordRequest = call.receive<ChangePasswordRequest>()
          if (changePasswordRequest.oldPassword.isNullOrBlank() || changePasswordRequest.newPassword.isNullOrBlank()) {
            throw Exception("Request parameter error: Too few arguments")
          }
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@put
        }

        DBConnection.database?.let { database ->
          val user = database.from(UserEntity).select().where {
            (UserEntity.id eq userId) and (UserEntity.deletedFlag neq true)
          }.orderBy(UserEntity.id.desc()).map {
            User.transform(it, includeSecret = true, includeHcm = false)
          }.firstOrNull()

          val doesPasswordMatch = BCrypt.checkpw(changePasswordRequest.oldPassword, user?.password)
          if (!doesPasswordMatch) {
            call.returnParameterErrorResponse("The old password is incorrect")
            return@put
          }

          val affected = database.update(UserEntity) {
            set(it.password, User.hashPassword(changePasswordRequest.newPassword))
            set(it.updatedAt, Instant.now())
            where {
              (it.id eq userId) and (it.deletedFlag neq true)
            }
          }
          if (affected == 0) {
            call.returnNotImplementedResponse()
          } else {
            val message = "You have successfully change your password"
            call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))
          }
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }
  }
}