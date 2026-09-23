package id.darno.core.mail

import io.ktor.server.config.*
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.util.*

class JakartaMailService(
    private val config: ApplicationConfig
) : MailService {

    private val logger = LoggerFactory.getLogger(MailService::class.java)

    override suspend fun send(to: String, subject: String, htmlBody: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", config.property("mail.smtp.auth").getString())
            put("mail.smtp.starttls.enable", config.property("mail.smtp.starttls").getString())
            put("mail.smtp.host", config.property("mail.smtp.host").getString())
            put("mail.smtp.port", config.property("mail.smtp.port").getString())
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(
                    config.property("mail.smtp.username").getString(),
                    config.property("mail.smtp.password").getString()
                )
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.property("mail.from").getString()))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            setContent(htmlBody, "text/html; charset=utf-8")
        }

        logger.debug("Send Email : {}", htmlBody)

        Transport.send(message)
    }
}