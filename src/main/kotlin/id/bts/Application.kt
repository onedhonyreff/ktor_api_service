package id.bts

import id.bts.config.GlobalConfig
import id.bts.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
  embeddedServer(
    Netty,
    port = GlobalConfig.config.property("application_port").getString().toInt(),
    host = GlobalConfig.config.property("application_host").getString(),
    module = Application::module
  ).start(wait = true)
}

fun Application.module() {
  configureJWTMiddleWare()
  configureSerialization()
  configureFirebase()
  configureWebSockets()
  configureRouting()
}