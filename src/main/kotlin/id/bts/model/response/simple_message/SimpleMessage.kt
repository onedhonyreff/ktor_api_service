package id.bts.model.response.simple_message

import kotlinx.serialization.Serializable

@Serializable
data class SimpleMessage(
  val message: String
)
