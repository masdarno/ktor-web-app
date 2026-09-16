package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.service.FileUploadException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.multipart.mapper.extractContent
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.toErrorMap
import id.darno.module.user.exception.UserException
import id.darno.module.user.helper.UserFormBuilder
import id.darno.module.user.model.DefaultValues
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.service.UserFileService
import id.darno.module.user.service.UserService
import id.darno.module.user.validator.UserProfileValidator
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

class UserProfileController(
    private val userService: UserService,
    private val userFileService: UserFileService
) {

    private val logger =
        LoggerFactory.getLogger(
            UserProfileController::class.java
        )

    private val protectedFiles =
        setOf(
            DefaultValues.DEFAULT_MALE_PHOTO,
            DefaultValues.DEFAULT_FEMALE_PHOTO
        )

    companion object {

        private const val TEMPLATE_PAGE =
            "pages/user/user-profile.html"

        private const val TEMPLATE_FORM =
            "pages/user/fragments/user-profile-form.html"

        private const val TEMPLATE_PHOTO =
            "pages/user/fragments/user-profile-photo.html"

        private const val PAGE_TITLE =
            "User Profile"
    }


    // =========================================================
    // PROFILE PAGE
    // =========================================================

    suspend fun index(
        call: ApplicationCall
    ) {

        val session =
            call.sessions.get<UserSession>()
                ?: return

        logger.info(
            "Get User Profile for user: {}",
            session.nama
        )

        val user =
            userService.getById(
                session.userId
            )

        val formData =
            mapOf(
                "id" to user.id,
                "nama" to user.nama,
                "alias" to user.alias,
                "email" to user.email,
                "photoUrl" to user.photoUrl
            )

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "formData" to formData
            )
        )
    }


    // =========================================================
    // UPDATE PROFILE DATA
    // =========================================================

    suspend fun update(
        call: ApplicationCall,
        userId: Short
    ) {

        logger.info(
            "Starting user profile data update for userId: {}",
            userId
        )

        // -----------------------------------------------------
        // 1. SESSION
        // -----------------------------------------------------

        val session =
            call.sessions.get<UserSession>()
                ?: return

        // -----------------------------------------------------
        // 2. ACCESS CHECK
        // -----------------------------------------------------

        if (userId != session.userId) {

            logger.warn(
                "Access denied: userId {} != session userId {}",
                userId,
                session.userId
            )

            call.hxTriggerWithToast(
                "Akses ditolak.",
                ToastType.ERROR
            )

            return
        }

        // -----------------------------------------------------
        // 3. RECEIVE NORMAL FORM
        // -----------------------------------------------------

        val parameters =
            call.receiveParameters()

        logger.debug(
            "Profile parameters: {}",
            parameters
        )

        // -----------------------------------------------------
        // 4. BUILD REQUEST
        // -----------------------------------------------------

        val request =
            UserFormBuilder.profile(
                parameters
            )

        // -----------------------------------------------------
        // 5. VALIDATE
        // -----------------------------------------------------

        val validationErrors =
            UserProfileValidator.validate(
                request
            )

        if (validationErrors.isNotEmpty()) {

            logger.warn(
                "Profile validation failed for userId {}: {}",
                userId,
                validationErrors
            )

            throw HtmxFormException(
                templatePath =
                    TEMPLATE_FORM,

                errors =
                    validationErrors.toErrorMap(),

                formData =
                    parameters.toFormData()
            )
        }

        // -----------------------------------------------------
        // 6. UPDATE DATABASE
        // -----------------------------------------------------

        try {

            val params =
                UpdateUserParams(
                    nama = request.nama,
                    alias = request.alias,
                    email = request.email
                )

            val updatedUser =
                userService.update(
                    userId,
                    params
                )

            logger.info(
                "User profile data updated successfully: {}",
                updatedUser.nama
            )

            // -------------------------------------------------
            // 7. UPDATE SESSION
            // -------------------------------------------------

            call.sessions.set(
                session.copy(
                    nama = updatedUser.nama
                )
            )

            // -------------------------------------------------
            // 8. SUCCESS
            // -------------------------------------------------

            call.hxTriggerWithToast(
                "Update User Profile Berhasil, ${updatedUser.nama}",
                ToastType.SUCCESS
            )

            call.respond(
                PebbleContent(
                    TEMPLATE_FORM,
                    mapOf(
                        "errors" to
                                emptyMap<String, String>(),

                        "formData" to
                                mapOf(
                                    "nama" to updatedUser.nama,
                                    "alias" to updatedUser.alias,
                                    "email" to updatedUser.email
                                )
                    )
                )
            )

        }
        catch (ex: UserException) {
            logger.error(
                "Failed to update user profile data for userId: {}", userId, ex
            )

            val (field, message) = when (ex) {
                is UserException.UsernameAlreadyExists -> "username" to ex.message
                is UserException.EmailAlreadyExists    -> "email" to ex.message
                is UserException.RoleNotFound          -> "roleId" to ex.message
                is UserException.GenderNotFound        -> "gender" to ex.message
            }
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    field to message
                ),
                formData = parameters.toFormData()
            )
        }
        catch (ex: ApplicationException) {

            logger.error(
                "Failed to update user profile data for userId: {}", userId, ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData = parameters.toFormData()
            )
        }
    }


    // =========================================================
    // UPLOAD PROFILE PHOTO
    // =========================================================

    suspend fun uploadPhoto(
        call: ApplicationCall,
        userId: Short
    ) {

        logger.info(
            "Starting profile photo upload for userId: {}",
            userId
        )

        // -----------------------------------------------------
        // 1. SESSION
        // -----------------------------------------------------

        val session =
            call.sessions.get<UserSession>()
                ?: return

        // -----------------------------------------------------
        // 2. ACCESS CHECK
        // -----------------------------------------------------

        if (userId != session.userId) {

            logger.warn(
                "Photo upload access denied: userId {} != session userId {}",
                userId,
                session.userId
            )

            call.hxTriggerWithToast(
                "Akses ditolak.",
                ToastType.ERROR
            )

            return
        }

        // -----------------------------------------------------
        // 3. RECEIVE MULTIPART
        // -----------------------------------------------------

        val multipart =
            call.receiveMultipart()

        val content =
            multipart.extractContent()

        val photoFile =
            content.files.firstOrNull {
                it.partName == "photoFile"
            }

        // -----------------------------------------------------
        // 4. FILE REQUIRED
        // -----------------------------------------------------

        if (photoFile == null) {

            throw HtmxFormException(
                templatePath =
                    TEMPLATE_PHOTO,

                errors =
                    mapOf(
                        "photoFile" to
                                "Foto harus dipilih"
                    ),

                formData =
                    mapOf(
                        "id" to userId.toString(),
                        "photoUrl" to session.photoUrl
                    )
            )
        }

        var uploadedPhotoName: String? = null

        try {

            // -------------------------------------------------
            // 5. GET EXISTING USER
            // -------------------------------------------------

            val existingUser =
                userService.getById(
                    userId
                )

            val oldPhotoName =
                existingUser.photo

            // -------------------------------------------------
            // 6. UPLOAD + VALIDATE FILE
            // -------------------------------------------------

            logger.info(
                "Uploading profile photo for userId: {}",
                userId
            )

            uploadedPhotoName =
                userFileService.uploadProfilePhoto(
                    photoFile
                )

            // -------------------------------------------------
            // 7. UPDATE DATABASE
            // -------------------------------------------------

            val updatedUser =
                userService.update(
                    userId,
                    UpdateUserParams(
                        photo =
                            uploadedPhotoName
                    )
                )

            logger.info(
                "Profile photo database update successful for userId: {}",
                userId
            )

            // -------------------------------------------------
            // 8. DELETE OLD PHOTO
            // -------------------------------------------------

            if (
                oldPhotoName.isNotBlank() &&
                oldPhotoName !in protectedFiles &&
                oldPhotoName != uploadedPhotoName
            ) {

                logger.info(
                    "Deleting old profile photo: {}",
                    oldPhotoName
                )

                userFileService.deleteProfilePhoto(
                    oldPhotoName
                )
            }

            // -------------------------------------------------
            // 9. UPDATE SESSION
            // -------------------------------------------------

            call.sessions.set(
                session.copy(
                    photoUrl =
                        updatedUser.photoUrl
                )
            )

            // -------------------------------------------------
            // 10. SUCCESS
            // -------------------------------------------------

            call.hxTriggerWithToast(
                "Foto profil berhasil diperbarui.",
                ToastType.SUCCESS
            )

            call.respond(
                PebbleContent(
                    TEMPLATE_PHOTO,
                    mapOf(
                        "errors" to
                                emptyMap<String, String>(),

                        "formData" to
                                mapOf(
                                    "id" to
                                            updatedUser.id,

                                    "photoUrl" to
                                            updatedUser.photoUrl
                                )
                    )
                )
            )

        } catch (ex: FileUploadException) {

            logger.error(
                "Profile photo upload failed for userId: {} - {}",
                userId,
                ex.message,
                ex
            )

            call.hxTriggerWithToast(
                "Upload foto GAGAL.",
                ToastType.ERROR
            )

            throw HtmxFormException(
                templatePath =
                    TEMPLATE_PHOTO,

                errors =
                    mapOf(
                        "photoFile" to
                                (
                                        ex.message
                                            ?: "Upload foto gagal"
                                        )
                    ),

                formData =
                    mapOf(
                        "id" to userId.toString(),
                        "photoUrl" to session.photoUrl
                    )
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to update profile photo for userId: {}",
                userId,
                ex
            )

            // -------------------------------------------------
            // ROLLBACK FILE
            // -------------------------------------------------

            if (uploadedPhotoName != null) {

                logger.info(
                    "Deleting uploaded photo after failed DB update: {}",
                    uploadedPhotoName
                )

                userFileService.deleteProfilePhoto(
                    uploadedPhotoName
                )
            }

            call.hxTriggerWithToast(
                "Upload foto GAGAL.",
                ToastType.ERROR
            )

            throw HtmxFormException(
                templatePath =
                    TEMPLATE_PHOTO,

                errors =
                    mapOf(
                        "photoFile" to
                                (
                                        ex.message
                                            ?: "Upload foto gagal"
                                        )
                    ),

                formData =
                    mapOf(
                        "id" to userId.toString(),
                        "photoUrl" to session.photoUrl
                    )
            )
        }
    }
}