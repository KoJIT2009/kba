package com.github.kojit2009.ktor.auth.bearer

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.request.ApplicationRequest
import io.ktor.response.respond

/**
 * Represents a Bearer authentication provider
 * @property name is the name of the provider, or `null` for a default provider
 */
class BearerAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val authenticationFunction = configuration.authenticationFunction

    /**
     * Bearer auth configuration
     */
    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
        internal var authenticationFunction: AuthenticationFunction<BearerAuthCredential> = {
            throw NotImplementedError(
                "Bearer auth validate function is not specified. Use Bearer { validate { ... } } to fix."
            )
        }

        /**
         * Sets a validation function that will check given [BearerAuthCredential] instance and return [Principal],
         * or null if credential does not correspond to an authenticated principal
         */
        fun validate(body: suspend ApplicationCall.(BearerAuthCredential) -> Principal?) {
            authenticationFunction = body
        }
    }
}

/**
 * Generates an Bearer challenge as a [HttpAuthHeader].
 */
fun bearerAuthChallenge(): HttpAuthHeader.Parameterized =
    HttpAuthHeader.Parameterized(
        "Bearer", LinkedHashMap<String, String>()
    )

/**
 * Installs Bearer Authentication mechanism
 */
fun Authentication.Configuration.bearer(
    name: String? = null,
    configure: BearerAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = BearerAuthenticationProvider(BearerAuthenticationProvider.Configuration(name).apply(configure))
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val credentials = call.request.bearerAuthenticationCredentials()
        val principal = credentials?.let { authenticate(call, it) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(BearerAuthenticationChallengeKey, cause) {
                call.respond(UnauthorizedResponse(bearerAuthChallenge()))
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

/**
 * Retrieves Bearer authentication credentials for this [ApplicationRequest]
 */
fun ApplicationRequest.bearerAuthenticationCredentials(): BearerAuthCredential? {
    when (val authHeader = parseAuthorizationHeader()) {
        is HttpAuthHeader.Single -> {
            if (!authHeader.authScheme.equals("Bearer", ignoreCase = true)) {
                return null
            }

            val authHeaderValue = try {
                authHeader.blob
            } catch (e: Throwable) {
                return null
            }

            return BearerAuthCredential(authHeaderValue)
        }
        else -> return null
    }
}

private val BearerAuthenticationChallengeKey: Any = "BearerAuth"