package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.http.mapper.toFormData
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.role.service.RoleService
import id.darno.module.user.helper.UserFormBuilder
import id.darno.module.user.mapper.toCreateUserParams
import id.darno.module.user.mapper.toUpdateUserParams
import id.darno.module.user.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class UserController(private val userService: UserService, private val roleService: RoleService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    companion object {
        private const val TEMPLATE_PAGE = "pages/user/users.html"
        private const val TEMPLATE_TABLE = "pages/user/fragments/users-table.html"
        private const val TEMPLATE_FORM = "pages/user/fragments/users-form.html"
        private const val PAGE_TITLE = "Daftar User"
    }

    suspend fun index(call: ApplicationCall) {

        val query = call.pagedQueryParameters()
        val result = userService.getUserTable(query)

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query
            ))
    }

    suspend fun table(call: ApplicationCall) {
        val query = call.pagedQueryParameters()
        val result = userService.getUserTable(query)

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

    suspend fun form(call: ApplicationCall) {

        val parameters = call.request.queryParameters
        val id = parameters["id"]?.toShortOrNull()
        val mode = parameters["mode"] ?: "add"

        val formData = id?.let { loadFormData(it) } ?: emptyMap()

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

    suspend fun create(call: ApplicationCall) {
        val parameters = call.receiveParameters()

        try {
            val request = UserFormBuilder.create(parameters)

            val user = userService.create(request.toCreateUserParams())

            logger.info("User created successfully: {}", user.nama)

            call.hxTriggerWithToast(
                "User ${user.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "user-saved")
            call.respond(HttpStatusCode.Created)

        } catch (ex: ConstraintViolationException) {
            // ERROR VALIDASI VALIKTOR
            logger.warn("Validation failed for user creation: {}", ex.constraintViolations)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        } catch (ex: ApplicationException) {
            // ERROR SERVICE/REPOSITORY
            logger.error("Failed to create user: {}", parameters["nama"], ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(mapErrorKey(ex) to (ex.message ?: "Ada kesalahan")),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }
    }

    suspend fun update(call: ApplicationCall, userId: Short) {
        val session = call.sessions.get<UserSession>()
            ?: return call.respondUniversalRedirect("/login")

        val parameters = call.receiveParameters()

        try {
            val request = UserFormBuilder.update(parameters)

            val user = userService.update(userId, request.toUpdateUserParams())

            if(userId == session.userId && session.roleId != user.roleId){
                call.sessions.set(
                    session.copy(
                        roleId = user.roleId,
                        role = user.role
                    )
                )
            }

            logger.info("User updated successfully: {} (id: {})", user.nama, userId)

            call.hxTriggerWithToast(
                "User ${user.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "user-saved")
            call.respond(HttpStatusCode.OK)

        } catch (ex: ConstraintViolationException) {
            // EROR VALIDASI VALIKTOR
            logger.warn("Validation failed for user update (id: $userId)", ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData() + ("id" to userId.toString()),
                formElement = formContext(),
                mode = "edit"
            )
        } catch (ex: ApplicationException) {
            // ERROR SERVICE/REPOSITORY
            logger.error("Failed to update user (id: $userId): ${parameters["nama"]}", ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(mapErrorKey(ex) to (ex.message ?: "Ada kesalahan")),
                formData = parameters.toFormData() + ("id" to userId.toString()),
                formElement = formContext(),
                mode = "edit"
            )
        }
    }

    suspend fun delete(call: ApplicationCall, id: Short) {
        try {
            userService.delete(id)
            call.hxTriggerWithToast(
                "User BERHASIL dihapus.",
                ToastType.SUCCESS,
                "user-deleted")
            call.respond(HttpStatusCode.NoContent)
        } catch (e: ApplicationException) {
            logger.error("Failed to delete user (id: {})", id, e)

            call.hxTriggerWithToast(
                "User GAGAL dihapus.",
                ToastType.ERROR)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    // HELPER
    private suspend fun formContext(): Map<String, Any> = mapOf(
        "roles" to roleService.getAll()
    )
    fun mapErrorKey(ex: ApplicationException): String {
        val msg = ex.message?.lowercase().orEmpty()
        return when {
            "username" in msg -> "username"
            "email" in msg -> "email"
            else -> "nama"
        }
    }

    private suspend fun loadFormData(userId: Short): Map<String, Any> {
        val user = userService.getById(userId)

        return mapOf(
            "id" to user.id,
            "nama" to user.nama,
            "alias" to user.alias,
            "username" to user.username,
            "email" to user.email,
            "roleId" to user.roleId
        )
    }
}