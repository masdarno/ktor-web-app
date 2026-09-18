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
import id.darno.module.wilayah.exception.KelurahanException
import id.darno.module.wilayah.helper.WilayahFormBuilder
import id.darno.module.wilayah.mapper.toCreateKelurahanParams
import id.darno.module.wilayah.mapper.toUpdateKelurahanParams
import id.darno.module.wilayah.service.KabupatenService
import id.darno.module.wilayah.service.KecamatanService
import id.darno.module.wilayah.service.KelurahanService
import id.darno.module.wilayah.service.ProvinsiService
import id.darno.module.wilayah.validator.CreateKelurahanValidator
import id.darno.module.wilayah.validator.UpdateKelurahanValidator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

class KelurahanController(
    private val kelurahanService: KelurahanService,
    private val kecamatanService: KecamatanService,
    private val kabupatenService: KabupatenService,
    private val provinsiService: ProvinsiService
) {

    private val logger =
        LoggerFactory.getLogger(
            KelurahanController::class.java
        )

    companion object {

        private const val TEMPLATE_PAGE =
            "pages/wilayah/kelurahan.html"

        private const val TEMPLATE_TABLE =
            "pages/wilayah/fragments/kelurahan-table.html"

        private const val TEMPLATE_FORM =
            "pages/wilayah/fragments/kelurahan-form.html"

        private const val TEMPLATE_FILTER_KABUPATEN =
            "pages/wilayah/fragments/kelurahan-filter-kabupaten.html"

        private const val TEMPLATE_FILTER_KECAMATAN =
            "pages/wilayah/fragments/kelurahan-filter-kecamatan.html"

        private const val PAGE_TITLE =
            "Daftar Kelurahan"

        private const val DEFAULT_PROVINSI_ID: Short =
            14

        private const val DEFAULT_KABUPATEN_ID: Short =
            226

        private const val DEFAULT_KECAMATAN_ID: Short =
            3270

        private const val NONE_KABUPATEN_ID: Short =
            -1

        private const val NONE_KECAMATAN_ID: Short =
            -1
    }

    suspend fun index(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val provinsiList =
            provinsiService.getAllActive()

        val requestedProvinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val selectedProvinsiId =
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

        val selectedKabupatenId =
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
                ?: NONE_KABUPATEN_ID

        val kecamatanList =
            if (selectedKabupatenId != NONE_KABUPATEN_ID) {
                kecamatanService
                    .getAllActiveByKabupaten(
                        selectedKabupatenId
                    )
            } else {
                emptyList()
            }

        val requestedKecamatanId =
            call.request.queryParameters["kecamatanId"]
                ?.toShortOrNull()

        val selectedKecamatanId =
            requestedKecamatanId
                ?.takeIf { id ->
                    kecamatanList.any { it.id == id }
                }
                ?: kecamatanList
                    .firstOrNull {
                        it.id == DEFAULT_KECAMATAN_ID
                    }
                    ?.id
                ?: kecamatanList
                    .firstOrNull()
                    ?.id
                ?: NONE_KECAMATAN_ID

        val result =
            kelurahanService.getTable(
                query,
                selectedKecamatanId
                    .takeIf {
                        it != NONE_KECAMATAN_ID
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
                "kecamatanList" to kecamatanList,
                "selectedProvinsiId" to selectedProvinsiId,
                "selectedKabupatenId" to selectedKabupatenId,
                "selectedKecamatanId" to selectedKecamatanId
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val kecamatanId =
            call.request.queryParameters["kecamatanId"]
                ?.toShortOrNull()
                ?.takeIf {
                    it != NONE_KECAMATAN_ID
                }

        val result =
            kelurahanService.getTable(
                query,
                kecamatanId
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

    suspend fun filterKabupaten(
        call: ApplicationCall
    ) {

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val kabupatenList =
            provinsiId
                ?.let {
                    kabupatenService
                        .getAllActiveByProvinsi(it)
                }
                ?: emptyList()

        val selectedKabupatenId =
            kabupatenList
                .firstOrNull()
                ?.id
                ?: NONE_KABUPATEN_ID

        call.respond(
            PebbleContent(
                TEMPLATE_FILTER_KABUPATEN,
                mapOf(
                    "kabupatenList" to
                            kabupatenList,
                    "selectedKabupatenId" to
                            selectedKabupatenId,
                    "autoLoad" to true
                )
            )
        )
    }

    suspend fun filterKecamatan(
        call: ApplicationCall
    ) {

        val kabupatenId =
            call.request.queryParameters["kabupatenId"]
                ?.toShortOrNull()

        val kecamatanList =
            kabupatenId
                ?.let {
                    kecamatanService
                        .getAllActiveByKabupaten(it)
                }
                ?: emptyList()

        val selectedKecamatanId =
            kecamatanList
                .firstOrNull()
                ?.id
                ?: NONE_KECAMATAN_ID

        call.respond(
            PebbleContent(
                TEMPLATE_FILTER_KECAMATAN,
                mapOf(
                    "kecamatanList" to
                            kecamatanList,
                    "selectedKecamatanId" to
                            selectedKecamatanId,
                    "autoLoad" to true
                )
            )
        )
    }

    suspend fun form(call: ApplicationCall) {

        val parameters =
            call.request.queryParameters

        val id =
            parameters["id"]?.toIntOrNull()

        val mode =
            parameters["mode"] ?: "add"

        val filterKecamatanId =
            parameters["kecamatanId"]
                ?.toShortOrNull()
                ?: DEFAULT_KECAMATAN_ID

        val formProvinsiId: Short
        val formKabupatenId: Short
        val formData: Map<String, Any>

        if (id != null) {

            val kelurahan =
                kelurahanService.getById(id)

            val kecamatan =
                kecamatanService.getById(
                    kelurahan.kecamatanId
                )

            val kabupaten =
                kabupatenService.getById(
                    kecamatan.kabupatenId
                )

            formKabupatenId =
                kabupaten.id

            formProvinsiId =
                kabupaten.provinsiId

            formData =
                mapOf(
                    "id" to kelurahan.id,
                    "provinsiId" to formProvinsiId,
                    "kabupatenId" to formKabupatenId,
                    "kecamatanId" to
                            kelurahan.kecamatanId,
                    "kode" to kelurahan.kode,
                    "nama" to kelurahan.nama,
                    "isActive" to kelurahan.isActive
                )

        } else {

            val kecamatan =
                kecamatanService.getById(
                    filterKecamatanId
                )

            val kabupaten =
                kabupatenService.getById(
                    kecamatan.kabupatenId
                )

            formKabupatenId =
                kabupaten.id

            formProvinsiId =
                kabupaten.provinsiId

            formData =
                mapOf(
                    "provinsiId" to
                            formProvinsiId,
                    "kabupatenId" to
                            formKabupatenId,
                    "kecamatanId" to
                            filterKecamatanId
                )
        }

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "errors" to
                            emptyMap<String, String>(),
                    "formData" to formData,
                    "formElement" to
                            formContext(
                                formProvinsiId,
                                formKabupatenId
                            )
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
            WilayahFormBuilder.createKelurahan(
                parameters
            )

        val validationErrors =
            CreateKelurahanValidator.validate(
                request
            )

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData(),
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "add"
            )
        }

        try {

            val kelurahan =
                kelurahanService.create(
                    request.toCreateKelurahanParams(
                        session.userId
                    )
                )

            call.hxTriggerWithToast(
                "Kelurahan ${kelurahan.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kelurahan-saved"
            )

            call.respond(
                HttpStatusCode.Created
            )

        }
        catch (ex: KelurahanException) {

            logger.error(
                "Failed to create kelurahan",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    ex.field to ex.message
                ),
                formData = parameters.toFormData(),
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "add"
            )
        }
        catch (ex: ApplicationException) {

            logger.error(
                "Failed to create kelurahan",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData = parameters.toFormData(),
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "add"
            )
        }
    }

    suspend fun update(
        call: ApplicationCall,
        id: Int
    ) {

        val parameters =
            call.receiveParameters()

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val request =
            WilayahFormBuilder.updateKelurahan(
                parameters
            )

        val validationErrors =
            UpdateKelurahanValidator.validate(
                request
            )

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData =
                    parameters.toFormData(
                        "id" to id
                    ),
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "edit"
            )
        }

        try {

            val kelurahan =
                kelurahanService.update(
                    id,
                    request.toUpdateKelurahanParams(
                        session.userId
                    )
                )

            logger.info(
                "Kelurahan updated successfully: {} (id: {})",
                kelurahan.nama,
                id
            )

            call.hxTriggerWithToast(
                "Kelurahan ${kelurahan.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kelurahan-saved"
            )

            call.respond(
                HttpStatusCode.OK
            )

        }
        catch (ex: KelurahanException) {

            logger.error(
                "Failed to update kelurahan (id: $id)",
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
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "edit"
            )
        }
        catch (ex: ApplicationException) {

            logger.error(
                "Failed to update kelurahan (id: $id)",
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
                formElement = formContext(
                    formProvinsiIdFrom(parameters),
                    formKabupatenIdFrom(parameters)
                ),
                mode = "edit"
            )
        }
    }

    suspend fun delete(
        call: ApplicationCall,
        id: Int
    ) {

        try {

            kelurahanService.delete(id)

            call.hxTriggerWithToast(
                "Kelurahan BERHASIL dihapus.",
                ToastType.SUCCESS,
                "kelurahan-deleted"
            )

            call.respond(
                HttpStatusCode.NoContent
            )

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to delete kelurahan (id: {})",
                id,
                ex
            )

            call.hxTriggerWithToast(
                ex.message
                    ?: "Kelurahan GAGAL dihapus.",
                ToastType.ERROR
            )

            call.respond(
                HttpStatusCode.NoContent
            )
        }
    }

    private fun formProvinsiIdFrom(
        parameters: Parameters
    ): Short =
        parameters["provinsiId"]
            ?.toShortOrNull()
            ?: DEFAULT_PROVINSI_ID

    private fun formKabupatenIdFrom(
        parameters: Parameters
    ): Short =
        parameters["kabupatenId"]
            ?.toShortOrNull()
            ?: DEFAULT_KABUPATEN_ID

    private suspend fun formContext(
        provinsiId: Short,
        kabupatenId: Short
    ): Map<String, Any> =
        mapOf(
            "provinsiList" to
                    provinsiService.getAllActive(),

            "kabupatenList" to
                    kabupatenService
                        .getAllActiveByProvinsi(
                            provinsiId
                        ),

            "kecamatanList" to
                    kecamatanService
                        .getAllActiveByKabupaten(
                            kabupatenId
                        )
        )
}