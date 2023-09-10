package id.bts.manager

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import id.bts.model.response.user.User
import io.ktor.server.config.*
import java.util.*

object TokenManager {
  private val config = HoconApplicationConfig(ConfigFactory.load())

  private val audience = config.property("audience").getString()
  private val secret = config.property("secret").getString()
  private val issuer = config.property("issuer").getString()
  val realm = config.property("realm").getString()
  private val expiration: Date
    get() {
      return Calendar.getInstance().also {
        it.add(Calendar.DAY_OF_YEAR, 1)
      }.time
    }

  fun generateToken(user: User): String {
    return JWT.create()
      .withAudience(audience)
      .withIssuer(issuer)
      .withClaim("id", user.id)
      .withClaim("email", user.email)
      .withClaim("name", user.name)
      .withClaim("is_super_visor", user.isSuperVisor)
      .withClaim("is_hcm", user.isHcm)
      .withExpiresAt(expiration)
      .sign(Algorithm.HMAC256(secret))
  }

  fun verifyJWTToken(): JWTVerifier {
    return JWT.require(Algorithm.HMAC256(secret))
      .withAudience(audience)
      .withIssuer(issuer)
      .build()
  }
}