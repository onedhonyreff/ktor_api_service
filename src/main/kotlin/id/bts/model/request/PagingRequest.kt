package id.bts.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PagingRequest(
  val page: Int = 0,
  val size: Int = 10,
) {
  val pagingOffset = this.page * this.size
}