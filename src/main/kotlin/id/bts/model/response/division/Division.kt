package id.bts.model.response.division

import id.bts.entities.DivisionEntity
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet

@Serializable
data class Division(
  val id: String?,
  val name: String?
) {
  companion object {
    fun transform(queryRowSet: QueryRowSet, divisionEntity: DivisionEntity = DivisionEntity): Division? {
      return queryRowSet[divisionEntity.id]?.toString()?.let {
        Division(
          id = it,
          name = queryRowSet[divisionEntity.name]
        )
      }
    }
  }
}
