package id.darno.module.auth.service

import id.darno.core.config.AppConfig
import id.darno.core.exceptions.ApplicationException
import id.darno.core.mail.MailService
import id.darno.core.security.crypto.Hasher
import id.darno.module.auth.helper.SecureRandomString
import id.darno.module.auth.repository.PasswordResetRepository
import id.darno.module.auth.repository.RememberMeRepository
import id.darno.module.user.service.UserAuthService
import id.darno.module.user.service.UserLookupService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

class PasswordResetServiceImpl(
    private val userLookupService: UserLookupService,
    private val userAuthService: UserAuthService,
    private val passwordResetRepository: PasswordResetRepository,
    private val rememberMeRepository: RememberMeRepository,
    private val mailService: MailService,
    private val appConfig: AppConfig,
    private val hasher: Hasher
) : PasswordResetService {

    override suspend fun requestReset(email: String) {
        val user = userLookupService.getByEmail(email) ?: return

        passwordResetRepository.deleteByUserId(user.id)

        val rawToken = SecureRandomString.generate(48)
        val tokenHash = hasher.hash(rawToken)
        val expiresAt = Clock.System.now().plus(24.hours).toLocalDateTime(TimeZone.UTC)

        passwordResetRepository.save(
            tokenHash = tokenHash,
            userId = user.id,
            expiresAt = expiresAt
        )

        val baseUrl = appConfig.baseUrl
        val resetLink = "$baseUrl/reset-password?token=$rawToken"

        mailService.send(
            to = user.email,
            subject = "Reset Password",
            htmlBody = """
                <p>Klik link berikut untuk reset password:</p>
                <p><a href="$resetLink">$resetLink</a></p>
                <p>Link berlaku 24 jam.</p>
            """.trimIndent()
        )
    }

    override suspend fun isTokenValid(token: String): Boolean {
        val tokenHash = hasher.hash(token)
        val record = passwordResetRepository.findValid(tokenHash)

        return record != null
    }

    override suspend fun resetPassword(token: String, newPassword: String): Boolean {
        val tokenHash = hasher.hash(token)
        val record = passwordResetRepository.findValid(tokenHash)
            ?: throw ApplicationException("Token tidak valid atau kadaluarsa")

        userAuthService.resetPassword(record.userId, newPassword)

        passwordResetRepository.markUsed(tokenHash)

        rememberMeRepository.deleteByUserId(record.userId)

        return true
    }
}