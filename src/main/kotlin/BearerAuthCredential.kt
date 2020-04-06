package com.github.kojit2009.ktor.auth.bearer

import io.ktor.auth.Credential

/**
 * Represents a user [token] credential
 * @property token
 */
data class BearerAuthCredential(val token: String) : Credential