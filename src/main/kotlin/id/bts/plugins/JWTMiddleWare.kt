package id.bts.plugins

import id.bts.manager.TokenManager
import id.bts.utils.Extensions.returnUnauthorizedResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureJWTMiddleWare() {
  install(Authentication) {
    jwt("user-authorization") {
      verifier(TokenManager.verifyJWTToken())
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
      verifier(TokenManager.verifyJWTToken())
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
  }
}
