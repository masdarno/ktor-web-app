package id.darno.module.auth.route

import id.darno.core.mail.MockMailStore
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.security.crypto.BCryptHasher
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.module.user.database.table.UserTable
import io.ktor.http.*
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

fun Route.configureDevelopmentRoute() {
    get("/dev/mails") {
        call.respondPebblePage(
            "pages/dev/mails.html",
            mapOf("mails" to MockMailStore.all())
        )
    }

    val hasher = BCryptHasher(logRounds = 10)
    route("/dev/update-password") {
        get{
            val csrfToken = call.ensureCsrfToken()
            call.respond(PebbleContent(
                "/pages/dev/update-password.html",
                mapOf(
                    "title" to "Update Password",
                    "csrfToken" to csrfToken
                )
            ))
        }
        post {
            try {
                var updatedCount = 0

                // Menjalankan transaksi database
                transaction {
                    val users = UserTable.selectAll().toList()

                    for (user in users) {
                        val userId = user[UserTable.id]
                        val username = user[UserTable.username]

                        // 1. Format password baru: username + "123"
                        val rawPassword = "${username}123"

                        // 2. Hash password menggunakan BCryptHasher
                        val hashedPassword = hasher.hash(rawPassword)

                        // 3. Update password di MariaDB
                        UserTable.update({ UserTable.id eq userId }) {
                            it[password] = hashedPassword
                        }

                        updatedCount++
                    }
                }

                // Kembalikan komponen HTML untuk HTMX
                call.respondText(
                    """
                <div style="color: green; font-weight: bold; padding: 10px; border: 1px solid green; background: #e6ffe6;">
                    Sukses! Berhasil memperbarui $updatedCount password user ke format BCrypt.
                </div>
                """.trimIndent(),
                    ContentType.Text.Html,
                    HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondText(
                    """
                <div style="color: red; font-weight: bold; padding: 10px; border: 1px solid red; background: #ffe6e6;">
                    Gagal: ${e.localizedMessage ?: "Terjadi kesalahan"}
                </div>
                """.trimIndent(),
                    ContentType.Text.Html,
                    HttpStatusCode.InternalServerError
                )
            }
        }
    }
}