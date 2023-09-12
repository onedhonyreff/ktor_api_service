package id.bts.model.response.user

import id.bts.entities.HumanCapitalManagementEntity
import id.bts.entities.SuperVisorEntity
import id.bts.entities.UserEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class User(
  @SerialName("id")
  val id: String,
  val email: String,
  val password: String? = null,
  val name: String,
  val photo: String?,
  val nip: String?,
  @SerialName("role_id")
  val roleId: String?,
  @SerialName("division_id")
  val divisionId: String?,
  @SerialName("department_id")
  val departmentId: String?,
  @SerialName("human_capital_management_id")
  val humanCapitalManagementId: String?,
  @SerialName("is_super_visor")
  val isSuperVisor: Boolean? = null,
  @SerialName("is_hcm")
  val isHcm: Boolean? = null,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?
) {
  companion object {
    fun hashPassword(password: String): String {
      return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun transform(queryRowSet: QueryRowSet, includeSecret: Boolean = false): User {
      return User(
        id = queryRowSet[UserEntity.id]!!.toString(),
        email = queryRowSet[UserEntity.email]!!,
        password = if (includeSecret) queryRowSet[UserEntity.password] else null,
        name = queryRowSet[UserEntity.name]!!,
        photo = queryRowSet[UserEntity.photo],
        nip = queryRowSet[UserEntity.nip],
        roleId = queryRowSet[UserEntity.roleId].toString(),
        divisionId = queryRowSet[UserEntity.divisionId].toString(),
        departmentId = queryRowSet[UserEntity.departmentId].toString(),
        humanCapitalManagementId = queryRowSet[UserEntity.humanCapitalManagementId].toString(),
        isSuperVisor = queryRowSet[SuperVisorEntity.id] != null,
        isHcm = queryRowSet[HumanCapitalManagementEntity.id] != null,
        createdAt = queryRowSet[UserEntity.createdAt].toString(),
        updatedAt = queryRowSet[UserEntity.updatedAt].toString(),
        deletedAt = queryRowSet[UserEntity.deletedAt].toString()
      )
    }
  }
}