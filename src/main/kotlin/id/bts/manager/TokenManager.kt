package id.bts.manager

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import id.bts.config.GlobalConfig
import id.bts.model.response.auth.Token
import id.bts.model.response.user.User
import java.util.*

object TokenManager {
  private val audience = GlobalConfig.config.property("jwt_audience").getString()
  val secret = GlobalConfig.config.property("jwt_secret").getString()
  private val issuer = GlobalConfig.config.property("jwt_issuer").getString()
  val secretForgotPassword = GlobalConfig.config.property("jwt_secret_forgot_password").getString()
  val realm = GlobalConfig.config.property("jwt_realm").getString()

  private fun getExpiration(type: Int = Calendar.DAY_OF_YEAR, amount: Int): Date {
    return Calendar.getInstance().also {
      it.add(type, amount)
    }.time
  }

  private fun createToken(user: User, asRefreshToken: Boolean = false): String {
    val jwtBuilder = JWT.create()
      .withAudience(audience)
      .withIssuer(issuer)
      .withClaim("id", user.id)
      .withClaim("email", user.email)
      .withClaim("name", user.name)
      .withClaim("is_super_visor", user.isSuperVisor)
      .withClaim("is_hcm", user.isHcm)
      .withClaim("is_refresh_token", asRefreshToken)

    if (!asRefreshToken) jwtBuilder.withExpiresAt(getExpiration(Calendar.DAY_OF_YEAR, 1))

    return jwtBuilder.sign(Algorithm.HMAC256(secret))
  }

  fun generateToken(user: User): Token {
    return Token(
      accessToken = createToken(user),
      refreshToken = createToken(user, true)
    )
  }

  fun createForgotPasswordToken(email: String, otp: String): String {
    val jwtBuilder = JWT.create()
      .withAudience(audience)
      .withIssuer(issuer)
      .withClaim("email", email)
      .withClaim("otp", otp)

    jwtBuilder.withExpiresAt(getExpiration(Calendar.MINUTE, 10))

    return jwtBuilder.sign(Algorithm.HMAC256(secretForgotPassword))
  }

  fun createJWTVerifier(signature: String = secret): JWTVerifier {
    return JWT.require(Algorithm.HMAC256(signature))
      .withAudience(audience)
      .withIssuer(issuer)
      .build()
  }

  fun decode(token: String, signature: String): DecodedJWT {
    return createJWTVerifier(signature).verify(token)
  }
}