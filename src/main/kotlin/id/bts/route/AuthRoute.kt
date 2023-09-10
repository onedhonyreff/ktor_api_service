package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.*
import id.bts.manager.TokenManager
import id.bts.model.request.auth.LoginRequest
import id.bts.model.request.auth.RegisterRequest
import id.bts.model.response.BaseResponse
import id.bts.model.response.auth.Login
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.User
import id.bts.utils.Extensions.returnFailedDatabaseResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import org.mindrot.jbcrypt.BCrypt

fun Application.configureAuthRoute() {
  routing {
    post("/register") {
      val registerRequest = try {
        call.receive<RegisterRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@post
      }

      try {
        DBConnection.database?.let { database ->
          val registeredUsersWithThisEmail = database.from(UserEntity).select().where {
            (UserEntity.email eq registerRequest.email) and (UserEntity.deletedFlag neq true)
          }.map { User.transform(it) }

          if (registeredUsersWithThisEmail.isNotEmpty()) {
            returnParameterErrorResponse("User already exist, please try a different email")
            return@post
          }

          val userId = database.insertAndGenerateKey(UserEntity) {
            set(it.email, registerRequest.email)
            set(it.password, User.hashPassword(registerRequest.password))
            set(it.name, registerRequest.name)
            set(it.roleId, registerRequest.roleId)
            set(it.divisionId, registerRequest.divisionId)
            set(it.departmentId, registerRequest.departmentId)
            set(it.humanCapitalManagementId, registerRequest.humanCapitalManagementId)
          } as Int

          database.from(LeaveTypeEntity).select().where {
            LeaveTypeEntity.deletedFlag neq true
          }.map { row ->
            val leaveTypeId = row[LeaveTypeEntity.id]
            val defaultDuration = row[LeaveTypeEntity.defaultDuration]
            database.insert(LeaveDurationEntity) {
              set(it.userId, userId)
              set(it.leaveTypeId, leaveTypeId)
              set(it.duration, defaultDuration)
            }
          }

          val message = "User registered successfully"
          call.respond(BaseResponse(success = true, message = message, data = SimpleMessage(message)))

        } ?: run { returnFailedDatabaseResponse() }

      } catch (e: Exception) {
        e.printStackTrace()
        returnParameterErrorResponse(e.message)
        return@post
      }
    }

    post("/login") {
      val loginRequest = try {
        call.receive<LoginRequest>()
      } catch (e: Exception) {
        returnParameterErrorResponse(e.message)
        return@post
      }

      try {
        DBConnection.database?.let { database ->
          val user = database.from(UserEntity)
            .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
            .leftJoin(HumanCapitalManagementEntity, on = HumanCapitalManagementEntity.userId eq UserEntity.id)
            .select().where {
              (UserEntity.email eq loginRequest.email) and (UserEntity.deletedFlag neq true)
            }.orderBy(UserEntity.id.desc()).map { User.transform(it, true) }.firstOrNull()
          if (user == null) {
            returnParameterErrorResponse("Email ${loginRequest.email} is not registered")
            return@post
          }

          val doesPasswordMatch = BCrypt.checkpw(loginRequest.password, user.password)
          if (!doesPasswordMatch) {
            returnParameterErrorResponse("The password is incorrect")
            return@post
          }

          val token = TokenManager.generateToken(user)
          val message = "Login successful"
          call.respond(
            BaseResponse(
              success = true,
              message = message,
              data = Login(email = user.email, token = token)
            )
          )

        } ?: run { returnFailedDatabaseResponse() }

      } catch (e: Exception) {
        e.printStackTrace()
        returnParameterErrorResponse(e.message)
        return@post
      }
    }

    authenticate("user-authorization") {
      get("/me") {
        call.principal<JWTPrincipal>()?.let { principal ->
          val email = principal.payload.getClaim("email").asString()
          val name = principal.payload.getClaim("name").asString()
          call.respond(
            BaseResponse(
              success = true,
              message = "Hello",
              data = SimpleMessage("Email: $email, Name: $name")
            )
          )
        }
      }
    }
  }
}