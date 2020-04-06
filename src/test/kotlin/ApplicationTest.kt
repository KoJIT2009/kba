package com.github.kojit2009.ktor.auth.bearer

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testAuthFail() {
        withTestApplication({ module() }) {
            handleRequest(HttpMethod.Get, "/protected/hello").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            handleRequest(HttpMethod.Get, "/protected/hello") {
                this.addHeader("Authorization", "Bearer test")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello user1", response.content)
            }

            handleRequest(HttpMethod.Get, "/protected/hello") {
                this.addHeader("Authorization", "Bearer test2")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }
}