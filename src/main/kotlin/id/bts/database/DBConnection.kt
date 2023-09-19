package id.bts.database

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

object DBConnection {
  private val config = HoconApplicationConfig(ConfigFactory.load())
  private val dbUrl = config.property("db_url").getString()
  private val dbDriver = config.property("db_driver").getString()
  private val dbUser = config.property("db_user").getString()
  private val dbPassword = config.property("db_password").getString()

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