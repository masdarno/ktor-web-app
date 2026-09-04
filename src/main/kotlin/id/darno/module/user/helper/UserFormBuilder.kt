package id.darno.module.user.helper

import id.darno.core.exceptions.service.BadRequestException
import id.darno.module.user.dto.CreateUserRequest
import id.darno.module.user.dto.UpdateUserRequest
import io.ktor.http.*

object UserFormBuilder {

    fun create(params: Parameters): CreateUserRequest {
        return CreateUserRequest(
            nama = params["name"].sanitize(),
            alias = params["alias"].sanitize(),
            username = params["username"].sanitizeLower(),
            email = params["email"].sanitizeLower(),
            roleId = params["roleId"]
                ?.toShortOrNull()
                ?: throw BadRequestException("Role wajib diisi")
        )
    }

    fun update(params: Parameters): UpdateUserRequest {
        return UpdateUserRequest(
            nama = params["name"].sanitize(),
            alias = params["alias"].sanitize(),
            username = params["username"].sanitizeLower(),
            email = params["email"].sanitizeLower(),
            roleId = params["roleId"]
                ?.toShortOrNull()
                ?: throw BadRequestException("Role wajib diisi")
        )
    }

    /* =========================
       Helpers
     ========================= */

    private fun String?.sanitize(): String =
        this?.trim().orEmpty()

    private fun String?.sanitizeLower(): String =
        this?.trim()?.lowercase().orEmpty()
}
