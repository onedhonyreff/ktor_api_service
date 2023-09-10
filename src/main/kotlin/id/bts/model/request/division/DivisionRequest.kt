package id.bts.model.request.division

import kotlinx.serialization.Serializable

@Serializable
data class DivisionRequest(
  val name: String
)
