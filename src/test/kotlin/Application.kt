package com.github.kojit2009.ktor.auth.bearer

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Application.module() {
    install(Authentication) {
        bearer("myBearerAuth") {
            validate { if (it.token == "test") UserIdPrincipal("user1") else null }
        }
    }

    install(Routing) {
        authenticate("myBearerAuth") {
            get("/protected/hello") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
    }
}