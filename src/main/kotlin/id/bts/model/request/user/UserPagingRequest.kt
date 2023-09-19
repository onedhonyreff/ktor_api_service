package id.bts.model.request.user

import id.bts.model.request.PagingRequest
import kotlinx.serialization.Serializable

@Serializable
data class UserPagingRequest(
  val search: String = ""
) : PagingRequest()