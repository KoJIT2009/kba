### INSTALL GRADLE 

```text
repositories {
    ...
    maven { url "https://jitpack.io" }
    ...
}

dependencies {
    ...
    implementation 'com.github.KoJIT2009:kba:1.0.1'
    ...
}
```

### HOW USE

```kotlin
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
```