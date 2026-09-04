package id.darno.module.auth.service

import id.darno.core.security.crypto.Hasher
import id.darno.core.session.model.RememberMeCookie
import id.darno.core.session.model.UserSession
import id.darno.module.auth.exception.AuthException
import id.darno.module.auth.helper.SecureRandomString
import id.darno.module.auth.repository.RememberMeRepository
import id.darno.module.unit.service.UnitService
import id.darno.module.user.model.RememberToken
import id.darno.module.user.service.UserLookupService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class RememberMeServiceImpl(
    private val rememberMeRepository: RememberMeRepository,
    private val userLookupService: UserLookupService,
    private val unitService: UnitService,
    private val hasher: Hasher
) : RememberMeService {

    override suspend fun authenticate(
        cookie: RememberMeCookie
    ): Result<UserSession> {

        val record = rememberMeRepository.findBySelector(cookie.selector)
            ?: return Result.failure(AuthException.TokenNotFound())

        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.UTC)

        if (record.expiresAt < now) {
            rememberMeRepository.delete(record.selector)
            return Result.failure(AuthException.TokenInvalid())
        }

        if (!hasher.verify(cookie.validator, record.validatorHash)) {
            rememberMeRepository.delete(record.selector)
            return Result.failure(AuthException.TokenInvalid())
        }

        val user = try {
            userLookupService.getById(record.userId)
        } catch (ex: Exception) {
            rememberMeRepository.delete(record.selector)
            return Result.failure(ex)
        }

        val unit = try {
            unitService.getById(record.unitId)
        } catch (ex: Exception) {
            rememberMeRepository.delete(record.selector)
            return Result.failure(ex)
        }

        if (!userLookupService.userHasUnit(user.id, unit.id)) {
            rememberMeRepository.delete(record.selector)
            return Result.failure(AuthException.UserUnitInvalid())
        }

        return Result.success(
            UserSession(
                user.id,
                user.nama,
                user.photoUrl,
                user.roleId,
                user.role,
                unit.id,
                unit.nama
            )
        )
    }

    override suspend fun issueToken(
        userId: Short,
        unitId: Short
    ): Pair<RememberToken, RememberMeCookie> {

        val selector = SecureRandomString.generate(12)
        val validator = SecureRandomString.generate(64)

        val expiresAt =
            Clock.System
                .now()
                .plus(30.days)
                .toLocalDateTime(TimeZone.currentSystemDefault())

        val token = RememberToken(
            selector = selector,
            userId = userId,
            unitId = unitId,
            validatorHash = hasher.hash(validator),
            expiresAt = expiresAt
        )

        return token to RememberMeCookie(selector, validator)
    }

    override suspend fun save(token: RememberToken) {
        rememberMeRepository.save(token)
    }

    override suspend fun revoke(selector: String) {
        rememberMeRepository.delete(selector)
    }

    override suspend fun revokeByUserId(id: Short) {
        rememberMeRepository.deleteByUserId(id)
    }
}
