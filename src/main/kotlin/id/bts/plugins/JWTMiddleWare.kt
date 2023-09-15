package id.bts.plugins

import id.bts.manager.TokenManager
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
      verifier(TokenManager.createJWTVerifier())
      realm = TokenManager.realm
      validate { jwtCredential ->
        if (
          jwtCredential.payload.getClaim("email").asString().isNotEmpty() &&
          jwtCredential.payload.getClaim("is_hcm").asBoolean() == true
        ) {
          JWTPrincipal(jwtCredential.payload)
        } else {
          null
        }
      }
      challenge { _, _ ->
        returnUnauthorizedResponse("Unauthorized access")
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
