package id.bts.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object GlobalConfig {
  val config = HoconApplicationConfig(ConfigFactory.load())
}