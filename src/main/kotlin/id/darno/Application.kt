package id.darno

import id.darno.core.database.configureDatabase
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.mail.InMemoryMailService
import id.darno.core.mail.JakartaMailService
import id.darno.core.mail.MailService
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.route.guard.authenticatedGuard
import id.darno.core.security.crypto.BCryptHasher
import id.darno.core.security.crypto.Hasher
import id.darno.core.security.crypto.Sha256Hasher
import id.darno.core.session.model.CsrfSession
import id.darno.core.session.model.RememberMeCookie
import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.core.storage.FileStorageService
import id.darno.core.storage.config.storageConfig
import id.darno.core.storage.local.LocalFileStorageService
import id.darno.module.auth.configureAuthDependencies
import id.darno.module.auth.configureAuthModule
import id.darno.module.auth.route.configureDevelopmentRoute
import id.darno.module.auth.service.RememberMeService
import id.darno.module.menu.configureMenuDependencies
import id.darno.module.menu.service.MenuCacheService
import id.darno.module.role.configureRoleDependencies
import id.darno.module.role.service.RoleMenuCacheService
import id.darno.module.unit.configureUnitDependencies
import id.darno.module.user.configureUserDependencies
import id.darno.module.user.configureUserModule
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.loader.FileLoader
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import kotlin.time.Duration.Companion.seconds

private val logger = LoggerFactory.getLogger("Application")

fun Application.module() {
    val config = this.environment.config

    // Infrasructure
    configureInfrastructure(config)
    // Startup Dependencies
    configureDependencies(config)
    // Security
    configureSecurity()
    // Routes Module
    configureRoutes()
    // Startup
    configureStartup()
}

fun Application.configureInfrastructure(config: ApplicationConfig){
    val isDev = config
        .propertyOrNull("ktor.deployment.development")
        ?.getString()
        ?.toBoolean()
        ?: false
    // CallLogging
    install(CallLogging) {
        level = if (isDev) Level.DEBUG else Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    // Pebble Template
    install(Pebble) {
        loader(
            if (isDev) {
                FileLoader(File("src/main/resources/pebble").absolutePath).apply {
                    charset = Charsets.UTF_8.name()

                }
            } else {
                ClasspathLoader().apply {
                    prefix = "pebble"
                    charset = Charsets.UTF_8.name()

                }
            }
        )
        cacheActive(!isDev)
    }
    // Database
    configureDatabase()
    // File Storage
    val storageConfig = storageConfig()
    dependencies{
        provide<FileStorageService> {
            LocalFileStorageService(storageConfig.local.basePath)
        }
    }
    // Static Resources
    configureStaticResources()
}

fun Application.configureDependencies(config: ApplicationConfig){
    val isDev = config
        .propertyOrNull("ktor.deployment.development")
        ?.getString()
        ?.toBoolean()
        ?: false
    dependencies {
        if(isDev){
            provide<MailService> { InMemoryMailService() }
        }else{
            provide<MailService> { JakartaMailService(config) }
        }
        // Password hasher (BCrypt)
        provide<Hasher>("bcrypt"){ BCryptHasher() }
        // Remember-me & reset-password token hasher (HMAC)
        provide<Hasher>("sha256"){
            Sha256Hasher(
                pepper = config
                    .property("security.rememberMePepper")
                    .getString()
            )
        }
    }
    configureMenuDependencies()
    configureRoleDependencies()
    configureUserDependencies()
    configureAuthDependencies()
    configureUnitDependencies()
}

fun Application.configureSecurity(){
    configureSessions()
    configureCSRF()
    configureRateLimit()
    configureRememberMe()
    configureAuthentication()
    configureStatusPages()
}

fun Application.configureRoutes(){
    routing {
        authenticatedGuard {
            get("/") {
                call.respondPebblePage("pages/blank.html", mapOf("title" to "Blank Page"))
            }
            get("/dashboard") {
                call.respondPebblePage("pages/blank.html", mapOf("title" to "Dashboard"))
            }
        }

        configureDevelopmentRoute()
    }

    configureUserModule()
    configureAuthModule()
}


fun Application.configureSessions() {
    val config = environment.config
    val secretEncryptKey = hex(config.property("security.session.encryptKey").getString())
    val secretSignKey = hex(config.property("security.session.signKey").getString())
    val isSecure = config.propertyOrNull("security.cookie.secure")?.getString()?.toBoolean() ?: false

    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = null // hilang saat browser ditutup
            cookie.httpOnly = true
            cookie.secure = isSecure
            cookie.extensions["SameSite"] = "Strict"
            transform(
                SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey)
            )
        }
        cookie<TempUserSession>("TEMP_USER_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = null
            cookie.httpOnly = true
            cookie.secure = isSecure
            cookie.extensions["SameSite"] = "Strict"
            transform(
                SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey)
            )
        }
        cookie<RememberMeCookie>("REMEMBER_ME") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = isSecure
            cookie.maxAgeInSeconds = 60L * 60 * 24 * 30
            cookie.extensions["SameSite"] = "Lax"
            transform(
                SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey)
            )
        }
        cookie<CsrfSession>("CSRF_SESSION") {
            cookie.httpOnly = true
            cookie.secure = isSecure
            cookie.extensions["SameSite"] = "Strict"
            transform(
                SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey)
            )
        }
    }
}

fun Application.configureCSRF() {
    val config = environment.config
    install(CSRF) {
        // tests Origin is an expected value
        val configOrigin = config.property("security.csrf.allowOrigin").getString()
        allowOrigin(configOrigin)

        // tests Origin matches Host header
        originMatchesHost()

        // custom header checks
        checkHeader("X-CSRF-Token") { csrfToken ->
            val session = sessions.get<CsrfSession>()
            csrfToken != null && csrfToken == session?.token
        }

        // Custom respon
        onFailure {
            respond(HttpStatusCode.Forbidden, "CSRF verification failed")
        }
    }
}

fun Application.configureRateLimit(){
    install(RateLimit) {
        // Named limiter khusus untuk login (misalnya batas 5 requests per menit per client)
        register(RateLimitName("login-limiter")) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)  // Refill 5 tokens setiap 60 detik
            requestKey { call ->
                call.request.origin.remoteHost  // Key berdasarkan IP client untuk identifikasi
            }
            requestWeight { call, key ->
                1  // Setiap request hitung sebagai 1 token
            }
        }
    }
}

fun Application.configureRememberMe() {
    val rememberMeService: RememberMeService by dependencies

    // Menggunakan fasa 'Plugins' atau secara eksplisit sebelum 'Authenticate'
    intercept(ApplicationCallPipeline.Plugins) {
        // Skip Assets
        val path = call.request.path()
        if (
            path.startsWith("/assets") ||
            path.startsWith("/css") ||
            path.startsWith("/js") ||
            path.startsWith("/vendors") ||
            path.startsWith("/uploads") ||
            path == "/favicon.ico"
        ) {
            return@intercept
        }

        // 1. Cek jika UserSession sudah ada (user sudah login di session ini)
        if (call.sessions.get<UserSession>() != null) return@intercept

        // 2. Jika session kosong, cek Cookie Remember Me
        val rememberCookie = call.sessions.get<RememberMeCookie>() ?: return@intercept

        rememberMeService.authenticate(rememberCookie)
            .onSuccess {
                // Set session baru ke dalam Ktor Sessions
                call.sessions.set(it)

                val (newToken, newCookie) =
                    rememberMeService.issueToken(
                        it.userId,
                        it.unitId
                    )

                rememberMeService.revoke(rememberCookie.selector)
                rememberMeService.save(newToken)
                call.sessions.set(newCookie)
            }
            .onFailure {
                rememberMeService.revoke(rememberCookie.selector)
                call.sessions.clear<RememberMeCookie>()
            }
    }
}

fun Application.configureAuthentication() {
    install(Authentication) {
        // Provider untuk UserSession (Login Completed)
        session<UserSession>("auth-user-session") {
            validate { session -> session }
            challenge { call.respondUniversalRedirect("/login") }
        }
        session<TempUserSession>("auth-temp-user-session") {
            validate { session -> session }
            challenge { call.respondUniversalRedirect("/login") }
        }

        session<TempUserSession>("auth-select-unit") {
            validate { session ->
                if(session.isVerified) { session } else null
            }
            challenge { session ->
                if ( session?.isVerified == false ) {
                    call.respondUniversalRedirect("/verify-email-required")
                } else {
                    call.respondUniversalRedirect("/login")
                }
            }
        }
        session<TempUserSession>("auth-verify-email") {
            validate { session ->
                if( ! session.isVerified) { session } else null
            }
            challenge { session ->
                if ( session?.isVerified == true ) {
                    call.respondUniversalRedirect("/select-unit")
                } else {
                    call.respondUniversalRedirect("/login")
                }
            }
        }
    }
}

fun Application.configureStatusPages(){
    install(StatusPages) {
        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            call.respond(
                HttpStatusCode.OK,
                PebbleContent(
                    "pages/auth/fragments/login-form.html",
                    mapOf(
                        "errors" to mapOf("username" to "Too many requests. Wait for $retryAfter seconds."),
                    )
                )
            )
        }
        exception<HtmxFormException> { call, cause ->
            logger.debug("Form submit failed {}", cause.errors)
            call.respond(
                HttpStatusCode.OK,
                PebbleContent(
                    cause.templatePath,
                    mapOf(
                        "mode" to cause.mode,
                        "errors" to cause.errors,
                        "formData" to cause.formData,
                        "formElement" to (cause.formElement ?: emptyMap<String, Any>())
                    )
                )
            )
        }
        // HTMX-aware Exception (APPLICATION ERROR)
        exception<Exception> { call, cause ->
            logger.error("Unhandled exception", cause)

            if (call.request.headers["HX-Request"] == "true") {
                call.hxTriggerWithToast(
                    message = "Terjadi kesalahan server",
                    type = ToastType.ERROR
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        // Global Throwable sebagai LAST RESORT
        exception<Throwable> { call, cause ->
            logger.error("Global Error: ${cause.message}", cause)
            throw cause // ⬅️ PENTING: rethrow
        }
    }
}

fun Application.configureStaticResources() {
    routing {
        staticResources("/assets", "static/assets")
        staticResources("/css", "static/css")
        staticResources("/js", "static/js")
        staticResources("/vendors", "static/vendors")

        // Akses via: /uploads/profile/namafile.jpg
        staticFiles("/uploads/profile", File("uploads/profile"))
    }
}

fun Application.configureStartup() {
    val menuCache: MenuCacheService by dependencies
    val roleMenuCache: RoleMenuCacheService by dependencies

    monitor.subscribe(ApplicationStarted) {
        launch {
            menuCache.init()
            roleMenuCache.init()
            log.info("All application caches initialized")
        }
    }
}
