package id.bts.model.response.on_going_project

import id.bts.entities.OnGoingProjectEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class OnGoingProject(
  val id: String?,
  @SerialName("leave_request_id")
  val leaveRequestId: String?,
  val name: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet): OnGoingProject {
      return OnGoingProject(
        id = queryRowSet[OnGoingProjectEntity.id].toString(),
        leaveRequestId = queryRowSet[OnGoingProjectEntity.leaveRequestId].toString(),
        name = queryRowSet[OnGoingProjectEntity.name]
      )
    }
  }
}
