package id.bts.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
  val success: Boolean,
  val message: String?,
  val data: T? = null
)
