package id.bts.model.request.department

import kotlinx.serialization.Serializable

@Serializable
data class DepartmentRequest(
  val name: String
)
