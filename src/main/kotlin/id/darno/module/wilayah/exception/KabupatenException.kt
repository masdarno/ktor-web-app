package id.darno.module.wilayah.exception

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.ServiceException
import id.darno.module.wilayah.database.table.KabupatenTable

sealed class KabupatenException(
    override val message: String,
    open val field: String = "general",
    cause: Throwable? = null
) : ServiceException(message, cause) {

    class KodeAlreadyExists(
        val kode: String,
        cause: Throwable? = null
    ) : KabupatenException("Kode $kode sudah ada", field = "kode", cause)

    class ProvinsiNotFound(
        val provinsiId: Short,
        cause: Throwable? = null
    ) : KabupatenException("Provinsi tidak ditemukan", field = "provinsiId", cause)

    class KabupatenInUse(
        cause: Throwable? = null
    ) : KabupatenException("Kabupaten masih memiliki data terkait di sistem", cause = cause)

}

fun DuplicateKeyException.toKabupatenException(
    kode: String? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KabupatenTable.KODE_UNIQUE_CONSTRAINT ->
            KabupatenException.KodeAlreadyExists(kode ?: "", cause = this)

        else -> this
    }

}

fun ForeignKeyException.toKabupatenException(
    provinsiId: Short? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KabupatenTable.PROVINSI_FK_CONSTRAINT ->
            KabupatenException.ProvinsiNotFound(provinsiId ?: 0, cause = this)

        else -> this
    }

}