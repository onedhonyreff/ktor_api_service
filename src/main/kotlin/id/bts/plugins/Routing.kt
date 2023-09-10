package id.bts.plugins

import id.bts.route.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing {
    get("/") {
      call.respond(HttpStatusCode.OK, "Welcome to Leave App API Service")
    }
  }
  configureFileRouting()
  configureDivisionRoute()
  configureDepartmentRoute()
  configureRoleRoute()
  configureLeaveTypeRoute()
  configureAuthRoute()
}
