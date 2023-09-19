package id.bts.utils

import id.bts.model.request.PagingRequest
import id.bts.model.request.leave_approval.AssignedApprovalPagingRequest
import id.bts.model.request.leave_request.SubmittedLeavePagingRequest
import id.bts.model.request.notification.NotificationPagingRequest
import id.bts.model.request.user.UserPagingRequest
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

  fun JWTChallengeContext.returnForbiddenResponse(message: String? = null) {
    runBlocking {
      call.respond(
        HttpStatusCode.Forbidden,
        BaseResponse(success = false, message = message ?: "Access is not permitted to your account", null)
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

  inline fun <reified T : PagingRequest> ApplicationCall.receivePagingRequest(): T {
    val pagingRequest: T = try {
      runBlocking {
        receive<T>()
      }
    } catch (e: Exception) {
      val defaultPagingRequest = when (T::class) {
        UserPagingRequest::class -> UserPagingRequest()
        AssignedApprovalPagingRequest::class -> AssignedApprovalPagingRequest()
        SubmittedLeavePagingRequest::class -> SubmittedLeavePagingRequest()
        NotificationPagingRequest::class -> NotificationPagingRequest()
        else -> PagingRequest()
      }
      defaultPagingRequest as T
    }
    return pagingRequest
  }
}