package id.darno.module.wilayah.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.toErrorMap
import id.darno.module.wilayah.exception.KabupatenException
import id.darno.module.wilayah.helper.WilayahFormBuilder
import id.darno.module.wilayah.mapper.toCreateKabupatenParams
import id.darno.module.wilayah.mapper.toUpdateKabupatenParams
import id.darno.module.wilayah.service.KabupatenService
import id.darno.module.wilayah.service.ProvinsiService
import id.darno.module.wilayah.validator.CreateKabupatenValidator
import id.darno.module.wilayah.validator.UpdateKabupatenValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.slf4j.LoggerFactory

class KabupatenController(
    private val kabupatenService: KabupatenService,
    private val provinsiService: ProvinsiService
) {

    private val logger =
        LoggerFactory.getLogger(KabupatenController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/wilayah/kabupaten.html"

        private const val TEMPLATE_TABLE =
            "pages/wilayah/fragments/kabupaten-table.html"

        private const val TEMPLATE_FORM =
            "pages/wilayah/fragments/kabupaten-form.html"

        private const val TEMPLATE_OPTIONS =
            "pages/wilayah/fragments/kabupaten-options.html"

        private const val PAGE_TITLE =
            "Daftar Kabupaten"

        private const val DEFAULT_PROVINSI_ID: Short =
            14
    }

    suspend fun index(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val requestedProvinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val provinsiList =
            provinsiService.getAllActive()

        val selectedProvinsiId =
            requestedProvinsiId
                ?.takeIf { id ->
                    provinsiList.any { it.id == id }
                }
                ?: DEFAULT_PROVINSI_ID

        val result =
            kabupatenService.getTable(
                query,
                selectedProvinsiId
            )

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query,
                "provinsiList" to provinsiList,
                "selectedProvinsiId" to selectedProvinsiId
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val result =
            kabupatenService.getTable(
                query,
                provinsiId
            )

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

        val parameters =
            call.request.queryParameters

        val id =
            parameters["id"]?.toShortOrNull()

        val mode =
            parameters["mode"] ?: "add"

        val filterProvinsiId =
            parameters["provinsiId"]?.toShortOrNull()
                ?: DEFAULT_PROVINSI_ID

        val formData =
            id?.let { loadFormData(it) }
                ?: mapOf(
                    "provinsiId" to filterProvinsiId
                )

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "errors" to emptyMap<String, String>(),
                    "formData" to formData,
                    "formElement" to formContext()
                )
            )
        )
    }

    suspend fun create(call: ApplicationCall) {

        val parameters =
            call.receiveParameters()

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val request =
            WilayahFormBuilder.createKabupaten(parameters)

        val validationErrors =
            CreateKabupatenValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }

        try {

            val kabupaten =
                kabupatenService.create(
                    request.toCreateKabupatenParams(
                        session.userId
                    )
                )

            call.hxTriggerWithToast(
                "Kabupaten ${kabupaten.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kabupaten-saved"
            )

            call.respond(HttpStatusCode.Created)

        }
        catch (ex: KabupatenException) {

            logger.error(
                "Failed to create kabupaten",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    ex.field to ex.message
                ),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }
        catch (ex: ApplicationException) {

            logger.error(
                "Failed to create kabupaten",
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

    suspend fun update(
        call: ApplicationCall,
        id: Short
    ) {

        val parameters =
            call.receiveParameters()

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val request =
            WilayahFormBuilder.updateKabupaten(parameters)

        val validationErrors =
            UpdateKabupatenValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData =
                    parameters.toFormData(
                        "id" to id
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }

        try {

            val kabupaten =
                kabupatenService.update(
                    id,
                    request.toUpdateKabupatenParams(
                        session.userId
                    )
                )

            logger.info(
                "Kabupaten updated successfully: {} (id: {})",
                kabupaten.nama,
                id
            )

            call.hxTriggerWithToast(
                "Kabupaten ${kabupaten.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kabupaten-saved"
            )

            call.respond(HttpStatusCode.OK)

        }
        catch (ex: KabupatenException) {

            logger.error(
                "Failed to update kabupaten (id: $id)",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    ex.field to ex.message
                ),
                formData =
                    parameters.toFormData(
                        "id" to id
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }
        catch (ex: ApplicationException) {

            logger.error(
                "Failed to update kabupaten (id: $id)",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData =
                    parameters.toFormData(
                        "id" to id
                    ),
                formElement = formContext(),
                mode = "edit"
            )
        }
    }

    suspend fun delete(
        call: ApplicationCall,
        id: Short
    ) {

        try {

            kabupatenService.delete(id)

            call.hxTriggerWithToast(
                "Kabupaten BERHASIL dihapus.",
                ToastType.SUCCESS,
                "kabupaten-deleted"
            )

            call.respond(HttpStatusCode.NoContent)

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to delete kabupaten (id: {})",
                id,
                ex
            )

            call.hxTriggerWithToast(
                ex.message
                    ?: "Kabupaten GAGAL dihapus.",
                ToastType.ERROR
            )

            call.respond(HttpStatusCode.NoContent)
        }
    }

    suspend fun options(call: ApplicationCall) {

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val selectedId =
            call.request.queryParameters["selected"]
                ?.toShortOrNull()
                ?: -1

        val kabupatenList =
            provinsiId
                ?.let {
                    kabupatenService.getAllActiveByProvinsi(it)
                }
                ?: emptyList()

        call.respond(
            PebbleContent(
                TEMPLATE_OPTIONS,
                mapOf(
                    "kabupatenList" to kabupatenList,
                    "selectedId" to selectedId
                )
            )
        )
    }

    private suspend fun formContext(): Map<String, Any> =
        mapOf(
            "provinsiList" to
                    provinsiService.getAllActive()
        )

    private suspend fun loadFormData(
        id: Short
    ): Map<String, Any> {

        val kabupaten =
            kabupatenService.getById(id)

        return mapOf(
            "id" to kabupaten.id,
            "provinsiId" to kabupaten.provinsiId,
            "kode" to kabupaten.kode,
            "nama" to kabupaten.nama,
            "isActive" to kabupaten.isActive
        )
    }
}