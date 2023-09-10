package id.bts.model.request.role

import kotlinx.serialization.Serializable

@Serializable
data class RoleRequest(
  val name: String
)
