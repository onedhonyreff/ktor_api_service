package id.bts.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
  val success: Boolean,
  val message: String?,
  val data: T? = null,
  @SerialName("total_data")
  val totalData: Int? = null
)
