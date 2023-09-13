package id.bts.model.response.auth

import kotlinx.serialization.Serializable

@Serializable
data class Login(
  val email: String,
  val token: Token
)
