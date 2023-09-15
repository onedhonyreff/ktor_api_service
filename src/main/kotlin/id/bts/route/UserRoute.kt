package id.bts.route

import id.bts.database.DBConnection
import id.bts.entities.*
import id.bts.model.response.BaseResponse
import id.bts.model.response.simple_message.SimpleMessage
import id.bts.model.response.user.User
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
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.ktorm.dsl.*
import org.ktorm.support.mysql.toLowerCase
import java.io.File
import java.time.Instant

fun Application.configureUserRoute() {
  routing {
    authenticate("user-authorization") {
      get("/my-profile") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        call.fetchUserProfile(userId)
      }

      get("/profile/{userId}") {
        val userId = try {
          call.parameters["userId"]!!.toInt()
        } catch (e: Exception) {
          call.returnParameterErrorResponse(e.message)
          return@get
        }

        call.fetchUserProfile(userId)
      }

      post("/users") {
        val pagingRequest = call.receivePagingRequest()

        DBConnection.database?.let { database ->
          val hcmUser = UserEntity.aliased(UserEntity.HCM_USER_ALIAS)
          val userHcmRelationEntity =
            HumanCapitalManagementEntity.aliased(HumanCapitalManagementEntity.USER_HCM_RELATION_ALIAS)
          val hcmSvRelationEntity = SuperVisorEntity.aliased(SuperVisorEntity.HCM_SV_RELATION_ALIAS)
          val hcmHcmRelationEntity =
            HumanCapitalManagementEntity.aliased(HumanCapitalManagementEntity.HCM_HCM_RELATION_ALIAS)
          val users = database.from(UserEntity)
            .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
            .leftJoin(DivisionEntity, on = DivisionEntity.id eq UserEntity.divisionId)
            .leftJoin(DepartmentEntity, on = DepartmentEntity.id eq UserEntity.departmentId)
            .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
            .leftJoin(userHcmRelationEntity, on = userHcmRelationEntity.userId eq UserEntity.id)
            .leftJoin(
              HumanCapitalManagementEntity,
              on = HumanCapitalManagementEntity.id eq UserEntity.humanCapitalManagementId
            )
            .leftJoin(hcmUser, on = hcmUser.id eq HumanCapitalManagementEntity.userId)
            .leftJoin(hcmSvRelationEntity, on = hcmSvRelationEntity.userId eq hcmUser.id)
            .leftJoin(hcmHcmRelationEntity, on = hcmHcmRelationEntity.userId eq hcmUser.id)
            .select(
              UserEntity.columns +
                RoleEntity.columns +
                DivisionEntity.columns +
                DepartmentEntity.columns +
                SuperVisorEntity.columns +
                HumanCapitalManagementEntity.columns +
                hcmUser.columns +
                userHcmRelationEntity.columns +
                hcmSvRelationEntity.columns +
                hcmHcmRelationEntity.columns
            )
            .limit(pagingRequest.size).offset(pagingRequest.pagingOffset)
            .where { ((UserEntity.email.toLowerCase() like "%${pagingRequest.search.lowercase()}%") or (UserEntity.name.toLowerCase() like "%${pagingRequest.search.lowercase()}%")) and (UserEntity.deletedFlag neq true) }
            .orderBy(UserEntity.id.desc())
            .map { User.transform(it, hcmRelationEntity = userHcmRelationEntity) }
          val message = "User list data fetched successfully"
          call.respond(BaseResponse(success = true, message = message, data = users, totalData = users.size))
        } ?: run { call.returnFailedDatabaseResponse() }
      }

      put("/my-profile") {
        val userId = try {
          call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString().toInt()
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

        var photoUrl: String? = null
        var name: String? = null
        var nip: String? = null
        var roleId: Int? = null
        var divisionId: Int? = null
        var departmentId: Int? = null
        var isPhotoRemoved = false

        try {
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FileItem -> {
                when (part.name) {
                  "photo" -> {
                    val inputStream = part.streamProvider()
                    val filename = part.originalFileName.replaceFileName()

                    val directory = File("${ProjectUtils.getProjectDir()}/assets/images/profile_photo")
                    if (!directory.exists()) {
                      directory.mkdir()
                    }

                    val photoPath = "$directory/$filename"
                    val destinationFile = File(photoPath)
                    inputStream.copyTo(destinationFile.outputStream().buffered())
                    photoUrl = "/file/open/image/profile_photo/$filename"
                  }
                }
              }

              is PartData.FormItem -> {
                when (part.name) {
                  "name" -> name = part.value
                  "nip" -> nip = part.value
                  "role_id" -> roleId = part.value.toInt()
                  "division_id" -> divisionId = part.value.toInt()
                  "department_id" -> departmentId = part.value.toInt()
                  "remove_photo" -> isPhotoRemoved = part.value.toBoolean()
                }
              }

              else -> Unit
            }
          }

          DBConnection.database?.let { database ->
            val affected = database.update(UserEntity) {
              name?.let { data -> set(it.name, data) }
              set(it.nip, nip)
              set(it.roleId, roleId)
              set(it.divisionId, divisionId)
              set(it.departmentId, departmentId)
              if (isPhotoRemoved) {
                set(it.photo, null)
              } else {
                photoUrl?.let { data -> set(it.photo, data) }
              }
              set(it.updatedAt, Instant.now())
              where {
                (it.id eq userId) and (it.deletedFlag neq true)
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
    }
  }
}

private fun ApplicationCall.fetchUserProfile(userId: Int) {
  runBlocking {
    DBConnection.database?.let { database ->
      val hcmUser = UserEntity.aliased(UserEntity.HCM_USER_ALIAS)
      val hcmRole = RoleEntity.aliased(RoleEntity.HCM_ROLE_ALIAS)
      val hcmDivision = DivisionEntity.aliased(DivisionEntity.HCM_DIVISION_ALIAS)
      val hcmDepartment = DepartmentEntity.aliased(DepartmentEntity.HCM_DEPARTMENT_ALIAS)
      val userHcmRelationEntity =
        HumanCapitalManagementEntity.aliased(HumanCapitalManagementEntity.USER_HCM_RELATION_ALIAS)
      val hcmSvRelationEntity = SuperVisorEntity.aliased(SuperVisorEntity.HCM_SV_RELATION_ALIAS)
      val hcmHcmRelationEntity =
        HumanCapitalManagementEntity.aliased(HumanCapitalManagementEntity.HCM_HCM_RELATION_ALIAS)
      database.from(UserEntity)
        .leftJoin(RoleEntity, on = RoleEntity.id eq UserEntity.roleId)
        .leftJoin(DivisionEntity, on = DivisionEntity.id eq UserEntity.divisionId)
        .leftJoin(DepartmentEntity, on = DepartmentEntity.id eq UserEntity.departmentId)
        .leftJoin(SuperVisorEntity, on = SuperVisorEntity.userId eq UserEntity.id)
        .leftJoin(userHcmRelationEntity, on = userHcmRelationEntity.userId eq UserEntity.id)
        .leftJoin(
          HumanCapitalManagementEntity,
          on = HumanCapitalManagementEntity.id eq UserEntity.humanCapitalManagementId
        )
        .leftJoin(hcmUser, on = hcmUser.id eq HumanCapitalManagementEntity.userId)
        .leftJoin(hcmRole, on = hcmRole.id eq hcmUser.roleId)
        .leftJoin(hcmDivision, on = hcmDivision.id eq hcmUser.divisionId)
        .leftJoin(hcmDepartment, on = hcmDepartment.id eq hcmUser.departmentId)
        .leftJoin(hcmSvRelationEntity, on = hcmSvRelationEntity.userId eq hcmUser.id)
        .leftJoin(hcmHcmRelationEntity, on = hcmHcmRelationEntity.userId eq hcmUser.id)
        .select(
          UserEntity.columns +
            RoleEntity.columns +
            DivisionEntity.columns +
            DepartmentEntity.columns +
            SuperVisorEntity.columns +
            HumanCapitalManagementEntity.columns +
            hcmUser.columns +
            hcmRole.columns +
            hcmDivision.columns +
            hcmDepartment.columns +
            userHcmRelationEntity.columns +
            hcmSvRelationEntity.columns +
            hcmHcmRelationEntity.columns
        )
        .where {
          (UserEntity.id eq userId) and (UserEntity.deletedFlag neq true)
        }.map { User.transform(it, hcmRelationEntity = userHcmRelationEntity) }.firstOrNull()?.let { data ->
          val message = "Profile data fetched successfully"
          respond(BaseResponse(success = true, message = message, data = data))
        } ?: run { returnNotFoundResponse() }
    } ?: run { returnFailedDatabaseResponse() }
  }
}