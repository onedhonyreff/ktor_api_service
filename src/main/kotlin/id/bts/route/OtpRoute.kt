package id.bts.route

import id.bts.database.DBConnection
import id.bts.manager.TokenManager
import id.bts.model.response.BaseResponse
import id.bts.model.response.auth.Token
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureOtpRoute() {
  routing {
    post("/otp-verification/forgot-password") {
      val email: String
      val otp: String
      try {
        val requestField = call.receiveParameters()
        email = requestField["email"]!!
        otp = requestField["otp"]!!

        // TODO remove this logic for real otp verification.
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
  }
}