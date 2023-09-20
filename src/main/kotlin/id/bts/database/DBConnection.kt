package id.bts.database

import id.bts.config.GlobalConfig
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

object DBConnection {
  private val dbUrl = GlobalConfig.config.property("db_url").getString()
  private val dbDriver = GlobalConfig.config.property("db_driver").getString()
  private val dbUser = GlobalConfig.config.property("db_user").getString()
  private val dbPassword = GlobalConfig.config.property("db_password").getString()

  val database = try {
    Database.connect(
      url = dbUrl,
      driver = dbDriver,
      user = dbUser,
      password = dbPassword,
      dialect = PostgreSqlDialect()
    )
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}