package id.bts.plugins

import id.bts.manager.TokenManager
import id.bts.utils.Extensions.returnForbiddenResponse
import id.bts.utils.Extensions.returnUnauthorizedResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureJWTMiddleWare() {
  install(Authentication) {
    jwt("user-authorization") {
      verifier(TokenManager.createJWTVerifier())
      realm = TokenManager.realm
      validate { jwtCredential ->
        if (jwtCredential.payload.getClaim("email").asString().isNotEmpty()) {
          JWTPrincipal(jwtCredential.payload)
        } else {
          null
        }
      }
      challenge { _, _ ->
        returnUnauthorizedResponse()
      }
    }

    jwt("admin-authorization") {
      var authorized = true
      verifier(TokenManager.createJWTVerifier())
      realm = TokenManager.realm
      validate { jwtCredential ->
        if (
          jwtCredential.payload.getClaim("email").asString().isNotEmpty() &&
          jwtCredential.payload.getClaim("is_hcm").asBoolean() == true
        ) {
          authorized = true
          JWTPrincipal(jwtCredential.payload)
        } else {
          authorized = false
          null
        }
      }
      challenge { _, _ ->
        if (authorized) {
          returnUnauthorizedResponse()
        } else {
          returnForbiddenResponse("Forbidden. Access only for HCM")
        }
        authorized = true
      }
    }

    jwt("approver-authorization") {
      var authorized = true
      verifier(TokenManager.createJWTVerifier())
      realm = TokenManager.realm
      validate { jwtCredential ->
        val payload = jwtCredential.payload
        if (
          payload.getClaim("email").asString().isNotEmpty() &&
          (payload.getClaim("is_hcm").asBoolean() == true || payload.getClaim("is_super_visor").asBoolean() == true)
        ) {
          authorized = true
          JWTPrincipal(jwtCredential.payload)
        } else {
          authorized = false
          null
        }
      }
      challenge { _, _ ->
        if (authorized) {
          returnUnauthorizedResponse()
        } else {
          returnForbiddenResponse("Forbidden. Access only for HCM or Super Visor")
        }
        authorized = true
      }
    }

    jwt("refresh-token") {
      verifier(TokenManager.createJWTVerifier())
      realm = TokenManager.realm
      validate { jwtCredential ->
        if (
          jwtCredential.payload.getClaim("email").asString().isNotEmpty() &&
          jwtCredential.payload.getClaim("is_refresh_token").asBoolean() == true
        ) {
          JWTPrincipal(jwtCredential.payload)
        } else {
          null
        }
      }
      challenge { _, _ ->
        returnUnauthorizedResponse("Invalid refresh token")
      }
    }
  }
}
