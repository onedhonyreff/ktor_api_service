package id.bts.database

import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

object DBConnection {
  val database = try {
    /* Local Server */
//    Database.connect(
//      url = "jdbc:postgresql:db_leave_app",
//      driver = "org.postgresql.Driver",
//      user = "postgres",
//      password = "dhony123",
//      dialect = PostgreSqlDialect()
//    )

    /* Online Server */
    Database.connect(
      url = "jdbc:postgresql://bubble.db.elephantsql.com:5432/pvhfrxwt",
      driver = "org.postgresql.Driver",
      user = "pvhfrxwt",
      password = "ky1wD1SqtG9IZLZpc6PHC2NvXyY-R30L"
    )
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}