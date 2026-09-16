package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.http.mapper.toFormData
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.report.helper.respondReport
import id.darno.core.session.model.UserSession
import id.darno.core.validation.toErrorMap
import id.darno.module.role.service.RoleService
import id.darno.module.user.exception.UserException
import id.darno.module.user.helper.UserFormBuilder
import id.darno.module.user.mapper.toCreateUserParams
import id.darno.module.user.mapper.toUpdateUserParams
import id.darno.module.user.service.UserService
import id.darno.module.user.validator.CreateUserValidator
import id.darno.module.user.validator.UpdateUserValidator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

class UserController(
    private val userService: UserService,
    private val roleService: RoleService
) {

    private val logger =
        LoggerFactory.getLogger(UserController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/user/users.html"

        private const val TEMPLATE_TABLE =
            "pages/user/fragments/users-table.html"

        private const val TEMPLATE_FORM =
            "pages/user/fragments/users-form.html"

        private const val PAGE_TITLE =
            "Daftar User"
    }

    // =========================================================
    // INDEX
    // =========================================================

    suspend fun index(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val result =
            userService.getUserTable(query)

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query
            )
        )
    }

    // =========================================================
    // TABLE
    // =========================================================

    suspend fun table(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val result =
            userService.getUserTable(query)

        call.respond(
            PebbleContent(
                TEMPLATE_TABLE,
                mapOf(
                    "result" to result,
                    "params" to query
                )
            )
        )
    }

    // =========================================================
    // FORM
    // =========================================================

    suspend fun form(call: ApplicationCall) {

        val parameters =
            call.request.queryParameters

        val id =
            parameters["id"]
                ?.toShortOrNull()

        val mode =
            parameters["mode"]
                ?: "add"

        val formData =
            id?.let { loadFormData(it) }
                ?: emptyMap()

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "formData" to formData,
                    "formElement" to formContext()
                )
            )
        )
    }

    // =========================================================
    // CREATE
    // =========================================================

    suspend fun create(call: ApplicationCall) {

        val parameters =
            call.receiveParameters()

        val request =
            UserFormBuilder.create(parameters)

        val validationErrors =
            CreateUserValidator.validate(request)

        if (validationErrors.isNotEmpty()) {

            logger.warn(
                "Validation failed for user creation: {}",
                validationErrors
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }

        try {

            val user =
                userService.create(
                    request.toCreateUserParams()
                )

            logger.info(
                "User created successfully: {}",
                user.nama
            )

            call.hxTriggerWithToast(
                "User ${user.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "user-saved"
            )

            call.respond(
                HttpStatusCode.Created
            )

        }
        catch (ex: UserException) {
            logger.error(
                "Failed to create user: {}",
                parameters["nama"],
                ex
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
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }
        catch (ex: ApplicationException) {
            logger.error(
                "Failed to create user: {}",
                parameters["nama"],
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    suspend fun update(
        call: ApplicationCall,
        userId: Short
    ) {

        val session =
            call.sessions.get<UserSession>()
                ?: return call.respondUniversalRedirect(
                    "/login"
                )

        val parameters =
            call.receiveParameters()

        val request =
            UserFormBuilder.update(parameters)

        val validationErrors =
            UpdateUserValidator.validate(request)

        if (validationErrors.isNotEmpty()) {

            logger.warn(
                "Validation failed for user update (id: {}): {}",
                userId,
                validationErrors
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData =
                    parameters.toFormData(
                        "id" to userId
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }

        try {

            val user =
                userService.update(
                    userId,
                    request.toUpdateUserParams()
                )

            /*
             * Jika user yang sedang login mengubah dirinya sendiri
             * dan role berubah, update session agar role/menu
             * langsung mengikuti data terbaru.
             */
            if (
                userId == session.userId &&
                session.roleId != user.roleId
            ) {

                call.sessions.set(
                    session.copy(
                        roleId = user.roleId,
                        role = user.role
                    )
                )
            }

            logger.info(
                "User updated successfully: {} (id: {})",
                user.nama,
                userId
            )

            call.hxTriggerWithToast(
                "User ${user.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "user-saved"
            )

            call.respond(
                HttpStatusCode.OK
            )

        }
        catch (ex: UserException) {
            logger.error(
                "Failed to update user (id: $userId): ${parameters["nama"]}",
                ex
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
                formData =
                    parameters.toFormData(
                        "id" to userId
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }
        catch (ex: ApplicationException) {
            logger.error(
                "Failed to update user (id: $userId): ${parameters["nama"]}",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData =
                    parameters.toFormData(
                        "id" to userId
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    suspend fun delete(
        call: ApplicationCall,
        id: Short
    ) {

        try {

            userService.delete(id)

            call.hxTriggerWithToast(
                "User BERHASIL dihapus.",
                ToastType.SUCCESS,
                "user-deleted"
            )

            call.respond(
                HttpStatusCode.NoContent
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to delete user (id: {})",
                id,
                ex
            )

            call.hxTriggerWithToast(
                "User GAGAL dihapus.",
                ToastType.ERROR
            )

            call.respond(
                HttpStatusCode.NoContent
            )
        }
    }

    // =========================================================
    // FORM CONTEXT
    // =========================================================

    private suspend fun formContext():
            Map<String, Any> =
        mapOf(
            "roles" to roleService.getAll()
        )

    // =========================================================
    // LOAD FORM DATA
    // =========================================================

    private suspend fun loadFormData(
        userId: Short
    ): Map<String, Any> {

        val user =
            userService.getById(userId)

        return mapOf(
            "id" to user.id,
            "nama" to user.nama,
            "alias" to user.alias,
            "username" to user.username,
            "email" to user.email,
            "roleId" to user.roleId
        )
    }

    // =========================================================
    // PDF
    // =========================================================

    suspend fun pdf(call: ApplicationCall) {

        try {

            val report =
                userService.generateReport(
                    search =
                        call.request
                            .queryParameters["search"]
                )

            call.respondReport(
                report = report
            )

        } catch (ex: Exception) {

            logger.error(
                "Gagal membuat laporan PDF user",
                ex
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                "Gagal membuat laporan PDF"
            )
        }
    }
}