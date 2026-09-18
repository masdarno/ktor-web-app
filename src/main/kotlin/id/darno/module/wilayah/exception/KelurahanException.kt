package id.darno.module.wilayah.exception

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.ServiceException
import id.darno.module.wilayah.database.table.KelurahanTable

sealed class KelurahanException(
    override val message: String,
    open val field: String = "general",
    cause: Throwable? = null
) : ServiceException(message, cause) {

    class KodeAlreadyExists(
        val kode: String,
        cause: Throwable? = null
    ) : KelurahanException("Kode $kode sudah ada", field = "kode", cause)

    class KecamatanNotFound(
        val kecamatanId: Short,
        cause: Throwable? = null
    ) : KelurahanException("Kecamatan tidak ditemukan", field = "kecamatanId", cause)

    class KelurahanInUse(
        cause: Throwable? = null
    ) : KelurahanException("Kelurahan masih memiliki data terkait di sistem", cause = cause)

}

fun DuplicateKeyException.toKelurahanException(
    kode: String? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KelurahanTable.KODE_UNIQUE_CONSTRAINT ->
            KelurahanException.KodeAlreadyExists(kode ?: "", cause = this)

        else -> this
    }

}

fun ForeignKeyException.toKelurahanException(
    kecamatanId: Short? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KelurahanTable.KECAMATAN_FK_CONSTRAINT ->
            KelurahanException.KecamatanNotFound(kecamatanId ?: 0, cause = this)

        else -> this
    }

}