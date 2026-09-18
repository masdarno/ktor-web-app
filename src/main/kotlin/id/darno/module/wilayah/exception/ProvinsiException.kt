package id.darno.module.wilayah.exception

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.service.ServiceException
import id.darno.module.wilayah.database.table.ProvinsiTable

sealed class ProvinsiException(
    override val message: String,
    open val field: String = "general",
    cause: Throwable? = null
) : ServiceException(message, cause) {

    class KodeAlreadyExists(
        val kode: String,
        cause: Throwable? = null
    ) : ProvinsiException("Kode $kode sudah ada", field = "kode", cause)

    class NamaAlreadyExists(
        val nama: String,
        cause: Throwable? = null
    ) : ProvinsiException("Nama $nama sudah ada", field = "nama", cause)

    class ProvinsiInUse(
        cause: Throwable? = null
    ) : ProvinsiException("Provinsi masih memiliki data terkait di sistem", cause = cause)

}

fun DuplicateKeyException.toProvinsiException(
    kode: String? = null,
    nama: String? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        ProvinsiTable.KODE_UNIQUE_CONSTRAINT ->
            ProvinsiException.KodeAlreadyExists(kode ?: "", cause = this)

        ProvinsiTable.NAMA_UNIQUE_CONSTRAINT ->
            ProvinsiException.NamaAlreadyExists(nama ?: "", cause = this)

        else -> this
    }

}