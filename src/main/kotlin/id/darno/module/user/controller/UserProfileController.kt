package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.service.FileUploadException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.model.DefaultValues
import id.darno.core.multipart.mapper.extractContent
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.user.dto.UpdateUserProfileRequest
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.service.UserFileService
import id.darno.module.user.service.UserService
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class UserProfileController(
    private val userService: UserService,
    private val userFileService: UserFileService
) {

    private val logger = LoggerFactory.getLogger(UserProfileController::class.java)
    private val protectedFiles = setOf(DefaultValues.DEFAULT_MALE_PHOTO, DefaultValues.DEFAULT_FEMALE_PHOTO)

    companion object {
        private const val TEMPLATE_PAGE = "pages/user/user-profile.html"
        private const val TEMPLATE_FORM = "pages/user/fragments/user-profile-form.html"
        private const val PAGE_TITLE = "User Profile"
    }

    suspend fun index(call: ApplicationCall) {

        val session = call.sessions.get<UserSession>()!!

        logger.info("Get User Profile for user: {}", session.name)

        val user = userService.getById(session.userId)

        val formData = mapOf(
            "id" to user.id,
            "name" to user.name,
            "alias" to user.alias,
            "email" to user.email,
            "photoUrl" to user.photoUrl
        )

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "formData" to formData
            ))
    }

    suspend fun update(call: ApplicationCall, userId: Short) {
        logger.info("Starting user profile update for userId: {}", userId)

        // Validasi session
        val session = call.sessions.get<UserSession>()
            ?: run {
                logger.warn("User session not found for userId: {}", userId)
                call.hxTriggerWithToast(
                    "Sesi pengguna tidak ditemukan.",
                    ToastType.ERROR
                )
                return
            }

        // Validasi akses
        if (userId != session.userId) {
            logger.warn(
                "Access denied: userId {} does not match session userId: {}",
                userId,
                session.userId
            )
            call.hxTriggerWithToast(
                "Akses ditolak: User ID tidak cocok dengan sesi.",
                ToastType.ERROR
            )
            return
        }

        // Extract multipart data
        val multipart = call.receiveMultipart()
        val content = multipart.extractContent()
        val photoFile = content.files.firstOrNull { it.partName == "photoFile" }
        val parameters = content.form

        logger.debug("Received parameters: {}", parameters)
        logger.debug("Photo file present: {}", photoFile != null)

        val formData = parameters.toFormData() + ("id" to userId.toString())

        var uploadedPhotoName: String? = null
        var oldPhotoName: String? = null

        try {
            // Build request object
            val request = UpdateUserProfileRequest(
                name = parameters["name"].orEmpty(),
                alias = parameters["alias"].orEmpty(),
                email = parameters["email"].orEmpty(),
            )

            logger.info("Processing update request for user: {}", request.name)

            // Ambil user SEKALI di awal
            val existingUser = userService.getById(userId)
            oldPhotoName = existingUser.photo

            // 1️⃣ UPLOAD FILE (jika ada)
            if (photoFile != null) {
                logger.info("Uploading photo file for userId: {}", userId)
                uploadedPhotoName = userFileService.uploadProfilePhoto(photoFile)
            }

            // Build update params
            val params = UpdateUserParams(
                name = request.name,
                alias = request.alias,
                email = request.email,
                photo = uploadedPhotoName
            )

            // Update user
            val updatedUser = userService.update(userId, params)
            logger.info("User profile updated successfully for user: {}", updatedUser.name)

            // 4. HAPUS FISIK SETELAH SUKSES UPDATE DB
            // Hanya hapus jika ada file baru yang diupload DAN user punya foto lama yang bukan default
            if (uploadedPhotoName != null &&
                oldPhotoName != null &&
                oldPhotoName !in protectedFiles
            ) {
                userFileService.deleteProfilePhoto(oldPhotoName)
            }

            // Update session photoUrl
            val newSession = session.copy(photoUrl = updatedUser.photoUrl)
            call.sessions.set(newSession)


            // Update form data dengan photoUrl
            val formData = formData + ("photoUrl" to updatedUser.photoUrl)

            call.hxTriggerWithToast(
                "Update User Profile Berhasil, ${updatedUser.name}",
                ToastType.SUCCESS
            )
            call.respond(
                PebbleContent(
                    TEMPLATE_FORM,
                    mapOf(
                        "errors" to emptyList<String>(),
                        "formData" to formData
                    )
                )
            )

        } catch (ex: ConstraintViolationException) {
            logger.warn("Validation failed for userId: $userId - {}", ex.constraintViolations)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = formData + ("photoUrl" to session.photoUrl)
            )
        } catch (ex: FileUploadException) {
            logger.error("File upload failed for userId: {} - {}", userId, ex.message, ex)

            call.hxTriggerWithToast("Update User Profile GAGAL!", ToastType.ERROR)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf("photoFile" to (ex.message ?: "Ada kesalahan")),
                formData = formData + ("photoUrl" to session.photoUrl)
            )
        } catch (ex: ApplicationException) {
            logger.error("Unexpected error during user profile update for userId: {}", userId, ex)

            // Hapus file baru jika DB gagal
            if (uploadedPhotoName != null) {
                logger.info("Deleting photo file for userId: {}", userId)
                userFileService.deleteProfilePhoto(uploadedPhotoName)
            }

            call.hxTriggerWithToast("Update User Profile GAGAL!", ToastType.ERROR)

            val key = when {
                ex.message?.contains("username", ignoreCase = true) == true -> "username"
                ex.message?.contains("email", ignoreCase = true) == true -> "email"
                else -> "name"
            }

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(key to (ex.message ?: "Ada kesalahan")),
                formData = formData + ("photoUrl" to session.photoUrl)
            )
        }
    }
}