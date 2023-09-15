package id.bts.model.response.user

import id.bts.entities.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class HCM(
  val id: String?,
  @SerialName("user_id")
  val userId: String?,
  val user: User?,
  @SerialName("created_at")
  val createdAt: String?,
  @SerialName("updated_at")
  val updatedAt: String?,
  @SerialName("deleted_at")
  val deletedAt: String?
) {
  companion object {
    fun transform(
      queryRowSet: QueryRowSet,
      userEntity: UserEntity = UserEntity,
      roleEntity: RoleEntity = RoleEntity,
      divisionEntity: DivisionEntity = DivisionEntity,
      departmentEntity: DepartmentEntity = DepartmentEntity,
      superVisorRelationEntity: SuperVisorEntity = SuperVisorEntity,
      hcmRelationEntity: HumanCapitalManagementEntity = HumanCapitalManagementEntity
    ): HCM? {
      return queryRowSet[HumanCapitalManagementEntity.id]?.toString()?.let {
        HCM(
          id = it,
          userId = queryRowSet[HumanCapitalManagementEntity.userId].toString(),
          user = User.transform(
            queryRowSet,
            includeHcm = false,
            userEntity = userEntity,
            roleEntity = roleEntity,
            divisionEntity = divisionEntity,
            departmentEntity = departmentEntity,
            superVisorRelationEntity = superVisorRelationEntity,
            hcmRelationEntity = hcmRelationEntity
          ),
          createdAt = queryRowSet[HumanCapitalManagementEntity.createdAt].toString(),
          updatedAt = queryRowSet[HumanCapitalManagementEntity.updatedAt].toString(),
          deletedAt = queryRowSet[HumanCapitalManagementEntity.deletedAt].toString()
        )
      }
    }
  }
}