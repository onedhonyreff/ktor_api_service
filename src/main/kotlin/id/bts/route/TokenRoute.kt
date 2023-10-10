package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.UserEntity
import id.bts.manager.TokenManager
import id.bts.model.response.BaseResponse
import id.bts.model.response.auth.Token
import id.bts.model.response.user.User
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import id.bts.utils.Extensions.returnUnauthorizedResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*

fun Application.configureTokenRoute() {
  routing {
    authenticate("refresh-token") {
      get("/refresh-token") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        DBConnection.database?.let { database ->
          val user = database.from(UserEntity).select().where {
            (UserEntity.id eq userId) and (UserEntity.deletedFlag neq true)
          }.map { User.transform(it) }.firstOrNull()

          if (user == null) {
            call.returnParameterErrorResponse("Unknown user")
            return@get
          }

          val token = TokenManager.generateToken(user)
          val message = "Token has been successfully refreshed"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = token
            )
          )
        } ?: run { call.returnFailedDatabaseResponse() }
      }
    }

    post("/refresh-token") {
      val oldToken: Token
      val isRefreshToken: Boolean
      val userId: Int
      try {
        oldToken = call.receive<Token>()
        val decodedToken = TokenManager.decode(oldToken.refreshToken!!, TokenManager.secret)
        isRefreshToken = decodedToken.getClaim("is_refresh_token").asBoolean()
        userId = decodedToken.getClaim("id").asString().toInt()
      } catch (e: Exception) {
        call.returnParameterErrorResponse(e.message)
        return@post
      }

      if (!isRefreshToken) {
        call.returnUnauthorizedResponse("Invalid refresh token")
        return@post
      }

      DBConnection.database?.let { database ->
        val user = database.from(UserEntity).select().where {
          (UserEntity.id eq userId) and (UserEntity.deletedFlag neq true)
        }.map { User.transform(it) }.firstOrNull()

        if (user == null) {
          call.returnParameterErrorResponse("Unknown user")
          return@post
        }

        val token = TokenManager.generateToken(user)
        val message = "Token has been successfully refreshed"
        call.respond(
          BaseResponse(
            success = true,
            message = message,
            data = token
          )
        )
      } ?: run { call.returnFailedDatabaseResponse() }
    }
  }
}