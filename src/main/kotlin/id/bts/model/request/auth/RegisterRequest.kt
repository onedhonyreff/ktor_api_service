package id.bts.model.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
  val email: String,
  val password: String,
  val name: String,
  @SerialName("role_id")
  val roleId: Int? = null,
  @SerialName("division_id")
  val divisionId: Int? = null,
  @SerialName("department_id")
  val departmentId: Int? = null,
  @SerialName("human_capital_management_id")
  val humanCapitalManagementId: Int? = null,
)
