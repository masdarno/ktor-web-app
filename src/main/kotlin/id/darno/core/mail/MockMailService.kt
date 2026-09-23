package id.darno.core.mail

import org.slf4j.LoggerFactory

class MockMailService : MailService {

    private val log = LoggerFactory.getLogger(MockMailService::class.java)

    override suspend fun send(
        to: String,
        subject: String,
        htmlBody: String
    ) {
        log.info(
            """
            |================ MOCK EMAIL =================
            |To      : $to
            |Subject : $subject
            |
            |$htmlBody
            |============================================
            """.trimMargin()
        )
    }
}
