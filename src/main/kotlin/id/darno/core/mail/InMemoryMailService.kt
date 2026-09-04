package id.darno.core.mail

data class MockMail(
    val to: String,
    val subject: String,
    val body: String,
    val sentAt: Long = System.currentTimeMillis()
)

object MockMailStore {
    private val mails = mutableListOf<MockMail>()

    fun add(mail: MockMail) {
        mails.add(mail)
    }

    fun all(): List<MockMail> = mails.toList()

    fun clear() = mails.clear()
}

class InMemoryMailService : MailService {

    override suspend fun send(
        to: String,
        subject: String,
        htmlBody: String
    ) {
        MockMailStore.add(
            MockMail(
                to = to,
                subject = subject,
                body = htmlBody
            )
        )
    }
}
