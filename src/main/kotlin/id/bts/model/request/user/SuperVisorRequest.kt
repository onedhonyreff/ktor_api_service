package id.bts.model.request.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuperVisorRequest(
  @SerialName("user_id")
  val userId: Int
)