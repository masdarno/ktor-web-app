package id.darno.module.auth.service

import id.darno.core.config.AppConfig
import id.darno.core.mail.MailService
import id.darno.core.security.crypto.Hasher
import id.darno.module.auth.helper.SecureRandomString
import id.darno.module.auth.repository.EmailVerificationRepository
import id.darno.module.user.service.UserEmailVerificationService
import io.ktor.server.config.*

class EmailVerificationServiceImpl(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val userEmailVerificationService: UserEmailVerificationService,
    private val mailService: MailService,
    private val appConfig: AppConfig,
    private val hasher: Hasher
) : EmailVerificationService {

    override suspend fun sendVerification(userId: Short, email: String) {
        val token = SecureRandomString.generate(32)

        emailVerificationRepository.save(
            token = hasher.hash(token),
            userId = userId
        )

        val baseUrl = appConfig.baseUrl
        val link = "$baseUrl/verify-email?token=$token"

        mailService.send(
            to = email,
            subject = "Verifikasi Email",
            htmlBody = """
                <p>Silakan verifikasi email Anda:</p>
                <a href="$link">Verifikasi Email</a>
            """.trimIndent()
        )
    }

    override suspend fun verify(token: String): Result<Unit> {
        val hashedToken = hasher.hash(token)
        val record = emailVerificationRepository.find(hashedToken)
            ?: return Result.failure(Exception("Token tidak valid"))

        userEmailVerificationService.markEmailVerified(record.userId)
        emailVerificationRepository.delete(token)

        return Result.success(Unit)
    }
}