package id.bts.utils

import id.bts.model.request.PagingRequest
import id.bts.model.response.BaseResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking

object Extensions {
  fun ApplicationCall.returnFailedDatabaseResponse(message: String? = null) {
    runBlocking {
      respond(
        HttpStatusCode.InternalServerError,
        BaseResponse(success = false, message = message ?: "Error with sever database", null)
      )
    }
  }

  fun ApplicationCall.returnNotFoundResponse(message: String? = null) {
    runBlocking {
      respond(
        HttpStatusCode.NotFound,
        BaseResponse(success = false, message = message ?: "Not Found", null)
      )
    }
  }

  fun ApplicationCall.returnParameterErrorResponse(message: String? = null) {
    runBlocking {
      respond(
        HttpStatusCode.BadRequest,
        BaseResponse(success = false, message = message ?: "Parameter Error", null)
      )
    }
  }

  fun ApplicationCall.returnNotImplementedResponse(message: String? = null) {
    runBlocking {
      respond(
        HttpStatusCode.NotImplemented,
        BaseResponse(success = false, message = message ?: "Changes not implemented", null)
      )
    }
  }

  fun ApplicationCall.returnUnauthorizedResponse(message: String? = null) {
    runBlocking {
      respond(
        HttpStatusCode.Unauthorized,
        BaseResponse(success = false, message = message ?: "Expired token or Unauthorized access", null)
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

  fun ApplicationCall.receivePagingRequest(): PagingRequest {
    val pagingRequest = try {
      runBlocking {
        receive<PagingRequest>()
      }
    } catch (e: Exception) {
      PagingRequest()
    }

    return pagingRequest
  }
}