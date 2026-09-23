package id.darno.core.mail

interface MailService {
    suspend fun send(
        to: String,
        subject: String,
        htmlBody: String
    )
}
