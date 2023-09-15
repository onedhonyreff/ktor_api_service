package id.bts.model.response.user

import id.bts.entities.*
import id.bts.model.response.department.Department
import id.bts.model.response.division.Division
import id.bts.model.response.role.Role
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
  val role: Role? = null,
  @SerialName("division_id")
  val divisionId: String?,
  val division: Division? = null,
  @SerialName("department_id")
  val departmentId: String?,
  val department: Department? = null,
  @SerialName("human_capital_management_id")
  val humanCapitalManagementId: String?,
  @SerialName("human_capital_management")
  val humanCapitalManagement: HCM? = null,
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

    fun transform(
      queryRowSet: QueryRowSet,
      includeSecret: Boolean = false,
      includeHcm: Boolean = true,
      userEntity: UserEntity = UserEntity,
      roleEntity: RoleEntity = RoleEntity,
      divisionEntity: DivisionEntity = DivisionEntity,
      departmentEntity: DepartmentEntity = DepartmentEntity,
      superVisorRelationEntity: SuperVisorEntity = SuperVisorEntity,
      hcmRelationEntity: HumanCapitalManagementEntity = HumanCapitalManagementEntity
    ): User {
      return User(
        id = queryRowSet[userEntity.id]!!.toString(),
        email = queryRowSet[userEntity.email]!!,
        password = if (includeSecret) queryRowSet[userEntity.password] else null,
        name = queryRowSet[userEntity.name]!!,
        photo = queryRowSet[userEntity.photo],
        nip = queryRowSet[userEntity.nip],
        roleId = queryRowSet[userEntity.roleId].toString(),
        role = Role.transform(queryRowSet, roleEntity = roleEntity),
        divisionId = queryRowSet[userEntity.divisionId].toString(),
        division = Division.transform(queryRowSet, divisionEntity = divisionEntity),
        departmentId = queryRowSet[userEntity.departmentId].toString(),
        department = Department.transform(queryRowSet, departmentEntity = departmentEntity),
        humanCapitalManagementId = queryRowSet[userEntity.humanCapitalManagementId].toString(),
        humanCapitalManagement = if (includeHcm) HCM.transform(
          queryRowSet,
          userEntity = UserEntity.aliased(UserEntity.HCM_USER_ALIAS),
          roleEntity = RoleEntity.aliased(RoleEntity.HCM_ROLE_ALIAS),
          divisionEntity = DivisionEntity.aliased(DivisionEntity.HCM_DIVISION_ALIAS),
          departmentEntity = DepartmentEntity.aliased(DepartmentEntity.HCM_DEPARTMENT_ALIAS),
          superVisorRelationEntity = SuperVisorEntity.aliased(SuperVisorEntity.HCM_SV_RELATION_ALIAS),
          hcmRelationEntity = HumanCapitalManagementEntity.aliased(HumanCapitalManagementEntity.HCM_HCM_RELATION_ALIAS)
        ) else null,
        isSuperVisor = queryRowSet[superVisorRelationEntity.id] != null,
        isHcm = queryRowSet[hcmRelationEntity.id] != null,
        createdAt = queryRowSet[userEntity.createdAt].toString(),
        updatedAt = queryRowSet[userEntity.updatedAt].toString(),
        deletedAt = queryRowSet[userEntity.deletedAt].toString()
      )
    }
  }
}