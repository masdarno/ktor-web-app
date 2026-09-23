package id.darno.module.wilayah.helper

import id.darno.core.exceptions.BadRequestException
import id.darno.module.wilayah.dto.CreateKabupatenRequest
import id.darno.module.wilayah.dto.CreateKecamatanRequest
import id.darno.module.wilayah.dto.CreateKelurahanRequest
import id.darno.module.wilayah.dto.CreateProvinsiRequest
import id.darno.module.wilayah.dto.UpdateKabupatenRequest
import id.darno.module.wilayah.dto.UpdateKecamatanRequest
import id.darno.module.wilayah.dto.UpdateKelurahanRequest
import id.darno.module.wilayah.dto.UpdateProvinsiRequest
import io.ktor.http.*

object WilayahFormBuilder {

    fun createProvinsi(params: Parameters): CreateProvinsiRequest {
        return CreateProvinsiRequest(
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize(),
            isActive = params["isActive"]?.toBoolean() ?: true
        )
    }

    fun updateProvinsi(params: Parameters): UpdateProvinsiRequest {
        return UpdateProvinsiRequest(
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize(),
            isActive = params["isActive"]?.toBoolean() ?: true
        )
    }

    fun createKabupaten(params: Parameters): CreateKabupatenRequest {
        return CreateKabupatenRequest(
            provinsiId = params["provinsiId"]?.toShortOrNull()
                ?: throw BadRequestException("Provinsi wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize()
        )
    }

    fun updateKabupaten(params: Parameters): UpdateKabupatenRequest {
        return UpdateKabupatenRequest(
            provinsiId = params["provinsiId"]?.toShortOrNull()
                ?: throw BadRequestException("Provinsi wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize(),
            isActive = params["isActive"]?.toBoolean() ?: true
        )
    }

    fun createKecamatan(params: Parameters): CreateKecamatanRequest {
        return CreateKecamatanRequest(
            kabupatenId = params["kabupatenId"]?.toShortOrNull()
                ?: throw BadRequestException("Kabupaten wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize()
        )
    }

    fun updateKecamatan(params: Parameters): UpdateKecamatanRequest {
        return UpdateKecamatanRequest(
            kabupatenId = params["kabupatenId"]?.toShortOrNull()
                ?: throw BadRequestException("Kabupaten wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize(),
            isActive = params["isActive"]?.toBoolean() ?: true
        )
    }

    fun createKelurahan(params: Parameters): CreateKelurahanRequest {
        return CreateKelurahanRequest(
            kecamatanId = params["kecamatanId"]?.toShortOrNull()
                ?: throw BadRequestException("Kecamatan wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize()
        )
    }

    fun updateKelurahan(params: Parameters): UpdateKelurahanRequest {
        return UpdateKelurahanRequest(
            kecamatanId = params["kecamatanId"]?.toShortOrNull()
                ?: throw BadRequestException("Kecamatan wajib diisi"),
            kode = params["kode"].sanitize(),
            nama = params["nama"].sanitize(),
            isActive = params["isActive"]?.toBoolean() ?: true
        )
    }

    /* =========================
       Helpers
     ========================= */

    private fun String?.sanitize(): String =
        this?.trim().orEmpty()
}