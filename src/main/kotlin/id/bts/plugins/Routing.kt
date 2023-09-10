package id.bts.plugins

import id.bts.route.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing {
    get("/") {
      call.respondText("Welcome to Leave App API Service")
    }
  }
  configureFileRouting()
  configureDivisionRoute()
  configureDepartmentRoute()
  configureRoleRoute()
  configureLeaveTypeRoute()
  configureAuthRoute()
}
