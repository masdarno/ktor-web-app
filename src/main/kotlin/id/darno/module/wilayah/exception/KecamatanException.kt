package id.darno.module.wilayah.exception

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.ServiceException
import id.darno.module.wilayah.database.table.KecamatanTable

sealed class KecamatanException(
    override val message: String,
    open val field: String = "general",
    cause: Throwable? = null
) : ServiceException(message, cause) {

    class KodeAlreadyExists(
        val kode: String,
        cause: Throwable? = null
    ) : KecamatanException("Kode $kode sudah ada", field = "kode", cause)

    class KabupatenNotFound(
        val kabupatenId: Short,
        cause: Throwable? = null
    ) : KecamatanException("Kabupaten tidak ditemukan", field = "kabupatenId", cause)

    class KecamatanInUse(
        cause: Throwable? = null
    ) : KecamatanException("Kecamatan masih memiliki data terkait di sistem", cause = cause)

}

fun DuplicateKeyException.toKecamatanException(
    kode: String? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KecamatanTable.KODE_UNIQUE_CONSTRAINT ->
            KecamatanException.KodeAlreadyExists(kode ?: "", cause = this)

        else -> this
    }

}

fun ForeignKeyException.toKecamatanException(
    kabupatenId: Short? = null
): ApplicationException {

    val key = constraint?.substringAfterLast(".")

    return when (key) {
        KecamatanTable.KABUPATEN_FK_CONSTRAINT ->
            KecamatanException.KabupatenNotFound(kabupatenId ?: 0, cause = this)

        else -> this
    }

}