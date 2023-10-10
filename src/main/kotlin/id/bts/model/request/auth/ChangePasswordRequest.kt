package id.bts.model.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
  val email: String? = null,
  @SerialName("old_password")
  val oldPassword: String? = null,
  @SerialName("new_password")
  val newPassword: String? = null,
  @SerialName("access_token")
  val accessToken: String? = null
)
