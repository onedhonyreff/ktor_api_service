package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.UserEntity
import id.bts.manager.TokenManager
import id.bts.model.request.auth.ChangePasswordRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.auth.Token
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.User
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnNotImplementedResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import id.bts.utils.Extensions.returnUnauthorizedResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import java.time.Instant

fun Application.configureUserPasswordRoute() {
  routing {
    post("/forgot-password") {
      val email = try {
        call.request.queryParameters["email"]!!
      } catch (e: Exception) {
        call.returnParameterErrorResponse(e.message)
        return@post
      }

      DBConnection.database?.let { database ->
        val isUserExists = database.from(UserEntity).select().where {
          (UserEntity.email eq email) and (UserEntity.deletedFlag neq true)
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

    post("/forgot-password/otp-verification") {
      val email: String
      val otp: String
      try {
        val requestField = call.receiveParameters()
        email = requestField["email"]!!
        otp = requestField["otp"]!!

        if (otp != "000000") throw Exception("Invalid OTP.")
      } catch (e: Exception) {
        call.returnParameterErrorResponse(e.message)
        return@post
      }

      DBConnection.database?.let { database ->
        // TODO otp verification.

        val forgotPasswordToken = Token(accessToken = TokenManager.createForgotPasswordToken(email, otp))
        val message = "Verified OTP code."
        call.respond(BaseResponse(success = true, message = message, data = forgotPasswordToken))
      } ?: run { call.returnFailedDatabaseResponse() }
    }

    put("/reset-password") {
      val email: String
      val otp: String
      val changePasswordRequest = try {
        val request = call.receive<ChangePasswordRequest>()
        if (request.newPassword == null) throw Exception("Required Field - New Password")
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
        if(otp != "000000") {
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
  }
}