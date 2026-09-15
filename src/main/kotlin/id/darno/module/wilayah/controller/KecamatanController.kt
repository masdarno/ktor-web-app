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
import id.darno.module.wilayah.helper.WilayahFormBuilder
import id.darno.module.wilayah.mapper.toCreateKecamatanParams
import id.darno.module.wilayah.mapper.toUpdateKecamatanParams
import id.darno.module.wilayah.service.KabupatenService
import id.darno.module.wilayah.service.KecamatanService
import id.darno.module.wilayah.service.ProvinsiService
import id.darno.module.wilayah.validator.CreateKecamatanValidator
import id.darno.module.wilayah.validator.UpdateKecamatanValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.slf4j.LoggerFactory

class KecamatanController(
    private val kecamatanService: KecamatanService,
    private val kabupatenService: KabupatenService,
    private val provinsiService: ProvinsiService
) {

    private val logger =
        LoggerFactory.getLogger(KecamatanController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/wilayah/kecamatan.html"

        private const val TEMPLATE_TABLE =
            "pages/wilayah/fragments/kecamatan-table.html"

        private const val TEMPLATE_FORM =
            "pages/wilayah/fragments/kecamatan-form.html"

        private const val TEMPLATE_FILTER_KABUPATEN =
            "pages/wilayah/fragments/kecamatan-filter-kabupaten.html"

        private const val PAGE_TITLE =
            "Daftar Kecamatan"

        // Default tampilan awal
        private const val DEFAULT_PROVINSI_ID: Short = 14
        private const val DEFAULT_KABUPATEN_ID: Short = 226

        private const val NONE_KABUPATEN_ID: Short = -1

        // Untuk Kelurahan
        private const val TEMPLATE_OPTIONS =
            "pages/wilayah/fragments/kecamatan-options.html"
    }

    suspend fun index(call: ApplicationCall) {

        val query = call.pagedQueryParameters()

        val provinsiList =
            provinsiService.getAllActive()

        val requestedProvinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val selectedProvinsiId: Short =
            requestedProvinsiId
                ?.takeIf { id ->
                    provinsiList.any { it.id == id }
                }
                ?: DEFAULT_PROVINSI_ID

        val kabupatenList =
            kabupatenService.getAllActiveByProvinsi(
                selectedProvinsiId
            )

        val requestedKabupatenId =
            call.request.queryParameters["kabupatenId"]
                ?.toShortOrNull()

        val selectedKabupatenId: Short =
            requestedKabupatenId
                ?.takeIf { id ->
                    kabupatenList.any { it.id == id }
                }
                ?: kabupatenList
                    .firstOrNull {
                        it.id == DEFAULT_KABUPATEN_ID
                    }
                    ?.id
                ?: kabupatenList
                    .firstOrNull()
                    ?.id
                ?: -1

        val result =
            kecamatanService.getTable(
                query,
                selectedKabupatenId.takeIf {
                    it != NONE_KABUPATEN_ID
                }
            )

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query,
                "provinsiList" to provinsiList,
                "kabupatenList" to kabupatenList,
                "selectedProvinsiId" to selectedProvinsiId,
                "selectedKabupatenId" to selectedKabupatenId
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val kabupatenId =
            call.request.queryParameters["kabupatenId"]
                ?.toShortOrNull()

        val result =
            kecamatanService.getTable(
                query,
                kabupatenId
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

    // Dipanggil saat dropdown Provinsi (filter tabel) berubah:
    // reload opsi Kabupaten + auto-refresh tabel via hx-trigger="load"
    suspend fun filterKabupaten(call: ApplicationCall) {

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val kabupatenList =
            provinsiId
                ?.let {
                    kabupatenService.getAllActiveByProvinsi(it)
                }
                ?: emptyList()

        val selectedKabupatenId: Short =
            kabupatenList
                .firstOrNull()
                ?.id
                ?: -1

        call.respond(
            PebbleContent(
                TEMPLATE_FILTER_KABUPATEN,
                mapOf(
                    "kabupatenList" to kabupatenList,
                    "selectedKabupatenId" to selectedKabupatenId,
                    "autoLoad" to true
                )
            )
        )
    }

    suspend fun form(call: ApplicationCall) {

        val parameters =
            call.request.queryParameters

        val id =
            parameters["id"]
                ?.toShortOrNull()

        val mode =
            parameters["mode"]
                ?: "add"

        val filterKabupatenId =
            parameters["kabupatenId"]
                ?.toShortOrNull()
                ?: DEFAULT_KABUPATEN_ID

        val formProvinsiId: Short
        val formData: Map<String, Any>

        if (id != null) {

            val kecamatan =
                kecamatanService.getById(id)

            val kabupaten =
                kabupatenService.getById(
                    kecamatan.kabupatenId
                )

            formProvinsiId =
                kabupaten.provinsiId

            formData =
                mapOf(
                    "id" to kecamatan.id,
                    "provinsiId" to formProvinsiId,
                    "kabupatenId" to kecamatan.kabupatenId,
                    "kode" to kecamatan.kode,
                    "nama" to kecamatan.nama,
                    "isActive" to kecamatan.isActive
                )

        } else {

            val kabupaten =
                kabupatenService.getById(
                    filterKabupatenId
                )

            formProvinsiId =
                kabupaten.provinsiId

            formData =
                mapOf(
                    "provinsiId" to formProvinsiId,
                    "kabupatenId" to filterKabupatenId
                )
        }

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "errors" to emptyMap<String, String>(),
                    "formData" to formData,
                    "formElement" to formContext(formProvinsiId)
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
            WilayahFormBuilder.createKecamatan(
                parameters
            )

        val validationErrors =
            CreateKecamatanValidator.validate(
                request
            )

        if (validationErrors.isNotEmpty()) {

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData(),
                formElement =
                    formContext(
                        formProvinsiIdFrom(parameters)
                    ),
                mode = "add"
            )
        }

        try {

            val kecamatan =
                kecamatanService.create(
                    request.toCreateKecamatanParams(
                        session.userId
                    )
                )

            call.hxTriggerWithToast(
                "Kecamatan ${kecamatan.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kecamatan-saved"
            )

            call.respond(
                HttpStatusCode.Created
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to create kecamatan",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to
                            (
                                    ex.message
                                        ?: "Ada kesalahan"
                                    )
                ),
                formData =
                    parameters.toFormData(),
                formElement =
                    formContext(
                        formProvinsiIdFrom(parameters)
                    ),
                mode = "add"
            )
        }
    }

    suspend fun update(
        call: ApplicationCall,
        id: Short
    ) {

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val parameters =
            call.receiveParameters()

        val request =
            WilayahFormBuilder.updateKecamatan(
                parameters
            )

        val validationErrors =
            UpdateKecamatanValidator.validate(
                request
            )

        if (validationErrors.isNotEmpty()) {

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData =
                    parameters.toFormData() +
                            ("id" to id.toString()),
                formElement =
                    formContext(
                        formProvinsiIdFrom(parameters)
                    ),
                mode = "edit"
            )
        }

        try {

            val kecamatan =
                kecamatanService.update(
                    id,
                    request.toUpdateKecamatanParams(
                        session.userId
                    )
                )

            logger.info(
                "Kecamatan updated successfully: {} (id: {})",
                kecamatan.nama,
                id
            )

            call.hxTriggerWithToast(
                "Kecamatan ${kecamatan.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kecamatan-saved"
            )

            call.respond(
                HttpStatusCode.OK
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to update kecamatan (id: $id)",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to
                            (
                                    ex.message
                                        ?: "Ada kesalahan"
                                    )
                ),
                formData =
                    parameters.toFormData() +
                            ("id" to id.toString()),
                formElement =
                    formContext(
                        formProvinsiIdFrom(parameters)
                    ),
                mode = "edit"
            )
        }
    }

    suspend fun delete(
        call: ApplicationCall,
        id: Short
    ) {

        try {

            kecamatanService.delete(id)

            call.hxTriggerWithToast(
                "Kecamatan BERHASIL dihapus.",
                ToastType.SUCCESS,
                "kecamatan-deleted"
            )

            call.respond(
                HttpStatusCode.NoContent
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to delete kecamatan (id: {})",
                id,
                ex
            )

            call.hxTriggerWithToast(
                ex.message
                    ?: "Kecamatan GAGAL dihapus.",
                ToastType.ERROR
            )

            call.respond(
                HttpStatusCode.NoContent
            )
        }
    }

    // Untuk Kelurahan
    suspend fun options(call: ApplicationCall) {

        val kabupatenId =
            call.request.queryParameters["kabupatenId"]
                ?.toShortOrNull()

        val selectedId: Short =
            call.request.queryParameters["selected"]
                ?.toShortOrNull()
                ?: -1

        val kecamatanList =
            kabupatenId
                ?.let {
                    kecamatanService
                        .getAllActiveByKabupaten(it)
                }
                ?: emptyList()

        call.respond(
            PebbleContent(
                TEMPLATE_OPTIONS,
                mapOf(
                    "kecamatanList" to kecamatanList,
                    "selectedId" to selectedId
                )
            )
        )
    }

    // HELPER

    private fun mapErrorKey(
        ex: ApplicationException
    ): String {

        val msg =
            ex.message
                ?.lowercase()
                .orEmpty()

        return when {
            "kabupaten" in msg -> "kabupatenId"

            "kode" in msg -> "kode"

            else -> "nama"
        }
    }

    private fun formProvinsiIdFrom(
        parameters: io.ktor.http.Parameters
    ): Short =
        parameters["provinsiId"]
            ?.toShortOrNull()
            ?: DEFAULT_PROVINSI_ID

    private suspend fun formContext(
        provinsiId: Short
    ): Map<String, Any> =
        mapOf(
            "provinsiList" to
                    provinsiService.getAllActive(),

            "kabupatenList" to
                    kabupatenService
                        .getAllActiveByProvinsi(
                            provinsiId
                        )
        )
}