package id.bts.utils

import id.bts.model.response.BaseResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking

object Extensions {
  fun PipelineContext<Unit, ApplicationCall>.returnFailedDatabaseResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.InternalServerError,
        BaseResponse(success = false, message = message ?: "Error with sever database", null)
      )
    }
  }

  fun PipelineContext<Unit, ApplicationCall>.returnNotFoundResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.NotFound,
        BaseResponse(success = false, message = message ?: "Not Found", null)
      )
    }
  }

  fun PipelineContext<Unit, ApplicationCall>.returnParameterErrorResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.BadRequest,
        BaseResponse(success = false, message = message ?: "Parameter Error", null)
      )
    }
  }

  fun PipelineContext<Unit, ApplicationCall>.returnNotImplementedResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.NotImplemented,
        BaseResponse(success = false, message = message ?: "Changes not implemented", null)
      )
    }
  }

  fun JWTChallengeContext.returnUnauthorizedResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.Unauthorized,
        BaseResponse(success = false, message = message ?: "Expired token or Unauthorized access", null)
      )
    }
  }

  fun String?.replaceFileName(replacement: String? = null, extension: String? = null): String {
    val fileName = replacement ?: System.currentTimeMillis().toString()
    val ext = extension ?: run {
      val split = this?.split(".")
      if (split != null && split.size >= 2) {
        split.last()
      } else null
    } ?: "jpg"

    return "${fileName}.${ext}"
  }
}